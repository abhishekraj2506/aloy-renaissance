package com.aloy.coreapp.service.impl;

import com.aloy.coreapp.client.OndcClient;
import com.aloy.coreapp.context.UserContext;
import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.exception.CoreServiceException;
import com.aloy.coreapp.model.Item;
import com.aloy.coreapp.model.Seller;
import com.aloy.coreapp.model.User;
import com.aloy.coreapp.model.UserCoupon;
import com.aloy.coreapp.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OndcClient ondcClient;

    @Autowired
    private RewardService rewardService;

    @Override
    public List<SellerDTO> getSellers() {
        List<Seller> sellers = sellerService.getActiveSellers();
        if (CollectionUtils.isEmpty(sellers)) {
            return new ArrayList<>();
        }
        List<SellerDTO> sellerDTOs = new ArrayList<>();
        sellers.forEach(s -> sellerDTOs.add(SellerDTO.builder()
                .id(s.getId())
                .bannerImageUrl(s.getDescriptor().getImages().get(0).getUrl())
                .rating(s.getRating() == null ? BigDecimal.valueOf(Math.random() * (5 - 3) + 3)
                        .setScale(2, RoundingMode.HALF_UP)
                        : s.getRating())
                .name(s.getName()).build()));
        return sellerDTOs;
    }

    @Override
    public List<SellerItemDTO> getSellerItems(String sellerId) {
        List<Item> items = itemService.getActiveItemsForSeller(sellerId);
        if (CollectionUtils.isEmpty(items)) return new ArrayList<>();
        List<SellerItemDTO> itemDTOs = new ArrayList<>();
        items.forEach(i -> itemDTOs.add(SellerItemDTO.builder()
                .itemId(i.getId())
                .name(i.getName())
                .sellerId(sellerId)
                .price(i.getPrice())
                .imageUrls(i.getImages().stream().map(OndcCommonDTO.Image::getUrl).collect(Collectors.toList()))
                .build()));
        return itemDTOs;
    }

    @Override
    public InitiateOrderResponseDTO initiateOrder(InitiateOrderRequestDTO initiateOrderRequestDTO) {
        Seller seller = sellerService.getSellerByAloyId(initiateOrderRequestDTO.getSellerId());
        List<String> itemIds = initiateOrderRequestDTO.getItems().stream()
                .map(InitiateOrderRequestDTO.CartItem::getItemId).toList();
        List<Item> itemsToBuy = itemService.getItemsByIds(itemIds);
        Map<String, String> itemIdToBppItemId = itemsToBuy.stream()
                .collect(Collectors.toMap(Item::getId, Item::getBppItemId));
        User user = userService.getById(UserContext.current().getUserId());
        User.Address address = user.getAddresses().stream()
                .filter(a -> a.getUuid().equals(initiateOrderRequestDTO.getUserAddressId()))
                .findFirst().orElseThrow(() -> new CoreServiceException("Customer address not found"));
        log.info("User request from {} {}", user.getId(), user.getName());
        OndcInitRequestDTO initRequestDTO = new OndcInitRequestDTO();
        OndcInitRequestDTO.Order order = new OndcInitRequestDTO.Order();
        order.setProvider(OndcCommonDTO.Provider.builder().id(seller.getBppProviderId()).build());
        order.setItems(initiateOrderRequestDTO.getItems().stream().map(i ->
                        OndcCommonDTO.Item.builder()
                                .id(itemIdToBppItemId.get(i.getItemId()))
                                .selected(OndcCommonDTO.Selected.builder()
                                        .quantity(OndcCommonDTO.Quantity.builder().count(i.getQuantity()).build()).build())
                                .build())
                .toList());
        order.setFulfillments(List.of(OndcCommonDTO.Fulfillment.builder()
                .customer(OndcCommonDTO.Customer.builder()
                        .person(OndcCommonDTO.Person.builder().name(user.getName()).build())
                        .contact(OndcCommonDTO.Contact.builder().phone(user.getPhoneNumber()).build())
                        .build())
                .type("Delivery")
                .stops(List.of(OndcCommonDTO.Stop.builder()
                        .contact(OndcCommonDTO.Contact.builder().phone(user.getPhoneNumber()).build())
                        .location(OndcCommonDTO.Location.builder()
                                .gps(address.getGps())
                                .address(address.getAddress())
                                .city(OndcCommonDTO.City.builder().name(address.getCity()).build())
                                .state(OndcCommonDTO.State.builder().name(address.getState()).build())
                                .country(OndcCommonDTO.Country.builder().code(address.getCountry()).build())
                                .area_code(address.getAreaCode())
                                .build())
                        .build())
                ).build()));
        order.setBilling(OndcCommonDTO.Billing.builder()
                .name(user.getName())
                .phone(user.getPhoneNumber())
                .address(address.getAddress())
                .city(OndcCommonDTO.City.builder().name(address.getCity()).build())
                .state(OndcCommonDTO.State.builder().name(address.getState()).build())
                .build());
        if (initiateOrderRequestDTO.getCouponId() != null) {
            UserCoupon userCoupon = rewardService.getUserCoupon(initiateOrderRequestDTO.getCouponId());
            order.setOffers(List.of(OndcCommonDTO.Offer.builder()
                    .id(userCoupon.getSellerCouponId())
                    .build()));
        }
        initRequestDTO.setMessage(OndcInitRequestDTO.Message.builder().order(order).build());
        String txnId = ondcClient.sendInitRequest(initRequestDTO, seller.getBppId(), seller.getBppUri());
        return InitiateOrderResponseDTO.builder().transactionId(txnId).build();
    }

    @Override
    public ConfirmOrderResponseDTO confirmOrder(ConfirmOrderRequestDTO confirmOrderRequestDTO) {
        Seller seller = sellerService.getSellerByAloyId(confirmOrderRequestDTO.getSellerId());
        List<String> itemIds = confirmOrderRequestDTO.getItems().stream()
                .map(InitiateOrderRequestDTO.CartItem::getItemId).toList();
        List<Item> itemsToBuy = itemService.getItemsByIds(itemIds);
        Map<String, String> itemIdToBppItemId = itemsToBuy.stream()
                .collect(Collectors.toMap(Item::getId, Item::getBppItemId));
        User user = userService.getById(UserContext.current().getUserId());
        User.Address address = user.getAddresses().stream()
                .filter(a -> a.getUuid().equals(confirmOrderRequestDTO.getUserAddressId()))
                .findFirst().orElseThrow(() -> new CoreServiceException("Customer address not found"));
        log.info("User request from {} {}", user.getId(), user.getName());
        OndcConfirmRequestDTO confirmRequestDTO = new OndcConfirmRequestDTO();
        OndcConfirmRequestDTO.Order order = new OndcConfirmRequestDTO.Order();
        order.setProvider(OndcCommonDTO.Provider.builder().id(seller.getBppProviderId()).build());
        order.setItems(confirmOrderRequestDTO.getItems().stream().map(i ->
                        OndcCommonDTO.Item.builder()
                                .id(itemIdToBppItemId.get(i.getItemId()))
                                .selected(OndcCommonDTO.Selected.builder()
                                        .quantity(OndcCommonDTO.Quantity.builder().count(i.getQuantity()).build()).build())
                                .build())
                .toList());
        order.setFulfillments(List.of(OndcCommonDTO.Fulfillment.builder()
                .customer(OndcCommonDTO.Customer.builder()
                        .person(OndcCommonDTO.Person.builder().name(user.getName()).build())
                        .contact(OndcCommonDTO.Contact.builder().phone(user.getPhoneNumber()).build())
                        .build())
                .type("Delivery")
                .stops(List.of(OndcCommonDTO.Stop.builder()
                        .contact(OndcCommonDTO.Contact.builder().phone(user.getPhoneNumber()).build())
                        .location(OndcCommonDTO.Location.builder()
                                .gps(address.getGps())
                                .address(address.getAddress())
                                .city(OndcCommonDTO.City.builder().name(address.getCity()).build())
                                .state(OndcCommonDTO.State.builder().name(address.getState()).build())
                                .country(OndcCommonDTO.Country.builder().code(address.getCountry()).build())
                                .area_code(address.getAreaCode())
                                .build())
                        .build())
                ).build()));
        order.setBilling(OndcCommonDTO.Billing.builder()
                .name(user.getName())
                .phone(user.getPhoneNumber())
                .address(address.getAddress())
                .city(OndcCommonDTO.City.builder().name(address.getCity()).build())
                .state(OndcCommonDTO.State.builder().name(address.getState()).build())
                .build());
        order.setPayments(List.of(OndcCommonDTO.Payment.builder()
                        .status("NOT-PAID")
                        .type("POST-FULFILLMENT")
                        .params(Map.of("amount", confirmOrderRequestDTO.getTotalAmount().toString(),
                                "currency", "INR"))
                .build()));
        if(confirmOrderRequestDTO.getCouponId() != null) {
            UserCoupon userCoupon = rewardService.getUserCoupon(confirmOrderRequestDTO.getCouponId());
            order.setOffers(List.of(OndcCommonDTO.Offer.builder()
                    .id(userCoupon.getSellerCouponId())
                    .build()));
        }
        confirmRequestDTO.setMessage(OndcConfirmRequestDTO.Message.builder().order(order).build());
        ondcClient.sendConfirmRequest(confirmRequestDTO, seller.getBppId(), seller.getBppUri(),
                confirmOrderRequestDTO.getOndcTransactionId());

        return ConfirmOrderResponseDTO.builder().ondcTransactionId(confirmOrderRequestDTO.getOndcTransactionId())
                .build();
    }

    @Override
    public boolean syncCatalog() {
        OndcSearchRequestDTO searchRequestDTO = new OndcSearchRequestDTO();
        searchRequestDTO.setMessage(OndcSearchRequestDTO.Message.builder()
                    .intent(OndcCommonDTO.Intent.builder()
                        .fulfillment(OndcCommonDTO.Fulfillment.builder()
                                .type("Delivery")
                        .stops(List.of(OndcCommonDTO.Stop.builder()
                                        .location(OndcCommonDTO.Location.builder()
                                        .gps("28.4594965,77.0266383").build()).build()))
                                .build()).build()).build());
        ondcClient.sendSearchRequest(searchRequestDTO, null, null);
        return true;
    }


}
