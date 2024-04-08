package com.aloy.sellerbppservice.service;

import com.aloy.sellerbppservice.dto.*;
import com.aloy.sellerbppservice.messaging.RabbitMessageProducer;
import com.aloy.sellerbppservice.model.*;
import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequestHandlerService {

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private RabbitMessageProducer messageProducer;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CouponService couponService;

    @Value("${aloy.bpp.id}")
    private String bppId;

    @Value("${aloy.bpp.uri}")
    private String bppUri;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public void handleSearch(OndcSearchRequestDTO ondcSearchRequestDTO) {
        OndcContextDTO context = ondcSearchRequestDTO.getContext();

        OndcSearchResponseDTO searchResponseDTO = new OndcSearchResponseDTO();
        OndcContextDTO responseContext = getResponseContext(context, "on_search");
        searchResponseDTO.setContext(responseContext);
        List<OndcCommonDTO.Provider> responseProviders = Lists.newArrayList();
        //send whole catalog
        List<Provider> providers = providerService.findActiveProviders();
        for (Provider provider : providers) {
            List<Item> itemsForProvider = itemService.getItemsForProvider(provider.getId());
            OndcCommonDTO.Provider p = new OndcCommonDTO.Provider();
            p.setId(provider.getId());
            p.setDescriptor(OndcCommonDTO.Descriptor.builder().name(provider.getName())
                    .images(provider.getDescriptor().getImages()).build());
            p.setCategories(provider.getCategories().stream()
                    .map(c -> OndcCommonDTO.Category.builder()
                            .id(c.getId())
                            .descriptor(OndcCommonDTO.Descriptor
                                    .builder().code(c.getDescriptor().getCode())
                                    .name(c.getDescriptor().getName())
                                    .build())
                            .build())
                    .collect(Collectors.toList()));
            p.setLocations(provider.getLocations().stream()
                    .map(l -> OndcCommonDTO.Location.builder()
                            .id(l.getId()).gps(l.getGps()).build())
                    .collect(Collectors.toList()));
            p.setFulfillments(provider.getFulfillments().stream()
                    .map(f -> OndcCommonDTO.Fulfillment.builder()
                            .id(f.getId()).type(f.getType()).build())
                    .collect(Collectors.toList()));
            p.setItems(itemsForProvider.stream()
                    .map(i -> OndcCommonDTO.Item.builder()
                            .id(i.getId()).category_id(i.getCategoryId())
                            .location_id(i.getLocationId()).fulfillment_id(i.getFulfillmentId())
                            .descriptor(OndcCommonDTO.Descriptor.builder().name(i.getName())
                                    .images(i.getImages()
                                            .stream().map(im -> OndcCommonDTO.Image.builder()
                                                    .url(im.getUrl()).build())
                                            .collect(Collectors.toList()))
                                    .short_desc(i.getShortDescription())
                                    .long_desc(i.getLongDescription()).build())
                            .matched(true)
                            .price(OndcCommonDTO.Price.builder()
                                    .currency(i.getPrice().getCurrency())
                                    .listed_value(i.getPrice().getListedValue())
                                    .value(i.getPrice().getValue())
                                    .build())
                            .build())
                    .collect(Collectors.toList()));
            responseProviders.add(p);
        }
        OndcSearchResponseDTO.Message message = new OndcSearchResponseDTO.Message();
        OndcCommonDTO.Catalog catalog = new OndcCommonDTO.Catalog();
        catalog.setDescriptor(OndcCommonDTO.Descriptor.builder().name("Aloy Test Seller BPP").build());
        catalog.setProviders(responseProviders);
        message.setCatalog(catalog);
        searchResponseDTO.setMessage(message);
        try {
            log.info("Response to search is {}", om.writeValueAsString(searchResponseDTO));
            messageProducer.sendMessage(om.writeValueAsString(searchResponseDTO));
        } catch (JsonProcessingException e) {
            log.error("Failed to send message to rabbitmq ", e);
        }
    }

    public void handleInit(OndcInitRequestDTO initRequestDTO) {
        log.info("Received init request {}", initRequestDTO.toString());
        OndcInitResponseDTO initResponseDTO = new OndcInitResponseDTO();
        OndcContextDTO context = initRequestDTO.getContext();
        OndcContextDTO responseContext = getResponseContext(context, "on_init");
        initResponseDTO.setContext(responseContext);

        List<OndcCommonDTO.Item> items = initRequestDTO.getMessage().getOrder().getItems();
        //Ideally here we would verify that all the items are available
        //For now we will assume that we have everything available
        Optional<Integer> totalItems = items.stream()
                .map(i -> i.getSelected().getQuantity().getCount()).reduce(Integer::sum);
        log.info("Total items needed by buyer {}", totalItems.orElse(0));

        OndcInitResponseDTO.Order responseOrder = new OndcInitResponseDTO.Order();
        responseOrder.setFulfillments(initRequestDTO.getMessage().getOrder().getFulfillments());
        responseOrder.setBilling(initRequestDTO.getMessage().getOrder().getBilling());

        List<OndcCommonDTO.Item> stockItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        String providerId = initRequestDTO.getMessage().getOrder().getProvider().getId();
        Provider provider = providerService.getById(providerId);
        if (provider != null) {
            responseOrder.setProvider(OndcCommonDTO.Provider.builder().id(providerId)
                    .descriptor(OndcCommonDTO.Descriptor.builder().name(provider.getName()).build())
                    .locations(provider.getLocations().stream()
                                    .map(l -> OndcCommonDTO.Location.builder()
                                            .id(l.getId()).gps(l.getGps()).build()).collect(Collectors.toList())    )
                    .build());
        }

        for (OndcCommonDTO.Item item : items) {
            Item i = itemService.getById(item.getId());
            if (i == null) continue;
            stockItems.add(OndcCommonDTO.Item.builder()
                    .id(i.getId()).category_id(i.getCategoryId())
                    .descriptor(OndcCommonDTO.Descriptor.builder().name(i.getName())
                            .images(i.getImages()
                                    .stream().map(im -> OndcCommonDTO.Image.builder()
                                            .url(im.getUrl()).build())
                                    .collect(Collectors.toList()))
                            .short_desc(i.getShortDescription())
                            .long_desc(i.getLongDescription()).build())
                    .build());
            totalAmount = totalAmount.add((new BigDecimal(i.getPrice().getListedValue())
                    .multiply(BigDecimal.valueOf(item.getSelected().getQuantity().getCount()))));
        }
        List<OndcCommonDTO.Offer> offers = initRequestDTO.getMessage().getOrder().getOffers();
        BigDecimal discount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(offers)) {
            String couponId = offers.get(0).getId();
            log.info("Offers available for this order {}", couponId);
                    Pair<BigDecimal, CouponUsage> discountFromCoupon = getDiscountFromCoupon(couponId, totalAmount, initResponseDTO);
            if (discountFromCoupon == null) return;
            discount = discountFromCoupon.getFirst();
            responseOrder.setOffers(offers);
        }
        log.info("Discount is {} on amount {}", discount, totalAmount);
        OndcCommonDTO.Quote quote = new OndcCommonDTO.Quote();
        BigDecimal tax = (totalAmount.subtract(discount)).multiply(BigDecimal.valueOf(0.05))
                .setScale(2, RoundingMode.HALF_UP);
        quote.setBreakup(Lists.newArrayList(
                OndcCommonDTO.Breakup.builder().title("base-price")
                        .price(OndcCommonDTO.Price.builder().currency("INR").value(totalAmount.toString()).build())
                        .build(),
                OndcCommonDTO.Breakup.builder().title("taxes")
                        .price(OndcCommonDTO.Price.builder().currency("INR").value(tax.toString()).build())
                        .build()
        ));
        if (!discount.equals(BigDecimal.ZERO)) {
            List<OndcCommonDTO.Breakup> breakup = quote.getBreakup();
            breakup.add(OndcCommonDTO.Breakup.builder().title("discount")
                    .price(OndcCommonDTO.Price.builder().currency("INR").value(discount.toString()).build())
                    .build());
            quote.setBreakup(breakup);
        }
        quote.setPrice(OndcCommonDTO.Price.builder().currency("INR")
                .value((totalAmount.add(tax).subtract(discount)).toString()).build());
        responseOrder.setQuote(quote);
        responseOrder.setItems(stockItems);
        responseOrder.setPayments(List.of(
                OndcCommonDTO.Payment.builder()
                        .type("PRE-FULFILLMENT")
                        .status("NOT-PAID")
                        .build()));
        initResponseDTO.setMessage(OndcInitResponseDTO.Message.builder().order(responseOrder).build());
        try {
            log.info("Response to init is {}", om.writeValueAsString(initResponseDTO));
            messageProducer.sendMessage(om.writeValueAsString(initResponseDTO));
        } catch (JsonProcessingException e) {
            log.error("Failed to send message to rabbitmq ", e);
        }

    }

    public void handleConfirm(OndcConfirmRequestDTO confirmRequestDTO) {
        log.info("Received confirm request {}", confirmRequestDTO.toString());

        OndcContextDTO context = confirmRequestDTO.getContext();
        OndcContextDTO responseContext = getResponseContext(context, "on_confirm");

        OndcConfirmResponseDTO confirmResponseDTO = new OndcConfirmResponseDTO();
        confirmResponseDTO.setContext(responseContext);

        OndcConfirmResponseDTO.Order responseOrder = new OndcConfirmResponseDTO.Order();
        Orders existingOrder = ordersService.getByOndcTransactionId(context.getTransaction_id());
        if (existingOrder != null) {
            responseOrder.setId(existingOrder.getId());
            responseOrder.setFulfillments(existingOrder.getFulfillments());
            responseOrder.setBilling(existingOrder.getBilling());
            responseOrder.setItems(existingOrder.getItems());
            responseOrder.setQuote(existingOrder.getQuote());
            responseOrder.setProvider(existingOrder.getProvider());
            responseOrder.setPayments(existingOrder.getPayments());
            responseOrder.setCancellation_terms(existingOrder.getCancellation_terms());
            confirmResponseDTO.setMessage(OndcConfirmResponseDTO.Message.builder().order(responseOrder).build());
            try {
                log.info("Order already exists, response to confirm is {}", om.writeValueAsString(confirmResponseDTO));
                messageProducer.sendMessage(om.writeValueAsString(confirmResponseDTO));
            } catch (JsonProcessingException e) {
                log.error("Failed to send message to rabbitmq ", e);
            }
            return;
        }

        responseOrder.setFulfillments(confirmRequestDTO.getMessage().getOrder().getFulfillments());
        responseOrder.setBilling(confirmRequestDTO.getMessage().getOrder().getBilling());

        List<OndcCommonDTO.Item> items = confirmRequestDTO.getMessage().getOrder().getItems();
        //Ideally here we would verify that all the items are available
        //For now we will assume that we have everything available
        Optional<Integer> totalItems = items.stream()
                .map(i -> i.getSelected().getQuantity().getCount()).reduce(Integer::sum);
        log.info("Total confirmed items needed by buyer {}", totalItems.orElse(0));

        List<OndcCommonDTO.Item> stockItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        String providerId = confirmRequestDTO.getMessage().getOrder().getProvider().getId();
        Provider provider = providerService.getById(providerId);
        if (provider != null) {
            responseOrder.setProvider(OndcCommonDTO.Provider.builder().id(providerId)
                    .descriptor(OndcCommonDTO.Descriptor.builder().name(provider.getName()).build())
                    .locations(provider.getLocations().stream()
                            .map(l -> OndcCommonDTO.Location.builder()
                                    .id(l.getId()).gps(l.getGps()).build()).collect(Collectors.toList())    )
                    .build());
        }

        for (OndcCommonDTO.Item item : items) {
            Item i = itemService.getById(item.getId());
            if (i == null) continue;
            int itemCount = item.getSelected().getQuantity().getCount();
            stockItems.add(OndcCommonDTO.Item.builder()
                    .id(i.getId()).category_id(i.getCategoryId())
                    .descriptor(OndcCommonDTO.Descriptor.builder().name(i.getName())
                            .images(i.getImages()
                                    .stream().map(im -> OndcCommonDTO.Image.builder()
                                            .url(im.getUrl()).build())
                                    .collect(Collectors.toList()))
                            .short_desc(i.getShortDescription())
                            .long_desc(i.getLongDescription()).build())
                    .selected(OndcCommonDTO.Selected.builder()
                            .quantity(OndcCommonDTO.Quantity.builder().count(itemCount).build())
                            .build())
                    .build());
            totalAmount = totalAmount.add((new BigDecimal(i.getPrice().getValue())
                    .multiply(BigDecimal.valueOf(itemCount))));
        }

        List<OndcCommonDTO.Offer> offers = confirmRequestDTO.getMessage().getOrder().getOffers();
        BigDecimal discount = BigDecimal.ZERO;
        CouponUsage couponUsage = null;
        if (CollectionUtils.isNotEmpty(offers)) {
            String couponId = offers.get(0).getId();
            Pair<BigDecimal, CouponUsage> discountFromCoupon = getDiscountFromCoupon(couponId, totalAmount, confirmResponseDTO);
            if (discountFromCoupon == null) return;
            discount = discountFromCoupon.getFirst();
            couponUsage = discountFromCoupon.getSecond();
            responseOrder.setOffers(offers);
        }
        OndcCommonDTO.Quote quote = new OndcCommonDTO.Quote();
        BigDecimal tax = (totalAmount.subtract(discount)).multiply(BigDecimal.valueOf(0.05))
                .setScale(2, RoundingMode.HALF_UP);
        quote.setBreakup(Lists.newArrayList(
                OndcCommonDTO.Breakup.builder().title("base-price")
                        .price(OndcCommonDTO.Price.builder().currency("INR").value(totalAmount.toString()).build())
                        .build(),
                OndcCommonDTO.Breakup.builder().title("taxes")
                        .price(OndcCommonDTO.Price.builder().currency("INR").value(tax.toString()).build())
                        .build()
        ));
        if (!discount.equals(BigDecimal.ZERO)) {
            List<OndcCommonDTO.Breakup> breakup = quote.getBreakup();
            breakup.add(OndcCommonDTO.Breakup.builder().title("discount")
                    .price(OndcCommonDTO.Price.builder().currency("INR").value(discount.toString()).build())
                    .build());
            quote.setBreakup(breakup);
        }
        quote.setPrice(OndcCommonDTO.Price.builder().currency("INR")
                .value((totalAmount.add(tax).subtract(discount)).toString()).build());
        responseOrder.setQuote(quote);
        responseOrder.setItems(stockItems);
        responseOrder.setPayments(List.of(
                OndcCommonDTO.Payment.builder()
                        .type("POST-FULFILLMENT")
                        .status("NOT-PAID")
                        .build()));

        responseOrder.setCancellation_terms(List.of(OndcCommonDTO.CancellationTerm.builder().return_eligible(false).build()));


        Orders customerOrder = new Orders();
        customerOrder.setBilling(responseOrder.getBilling());
        customerOrder.setOndcTransactionId(context.getTransaction_id());
        customerOrder.setCancellation_terms(responseOrder.getCancellation_terms());
        customerOrder.setFulfillments(responseOrder.getFulfillments());
        customerOrder.setItems(responseOrder.getItems());
        customerOrder.setQuote(responseOrder.getQuote());
        customerOrder.setProvider(responseOrder.getProvider());
        customerOrder.setPayments(responseOrder.getPayments());
        if (couponUsage != null) {
            customerOrder.setCouponUsageId(couponUsage.getCouponId());
        }
        customerOrder = ordersService.save(customerOrder);

        if(couponUsage != null) {
            couponUsage.setOrderId(customerOrder.getId());
            couponService.saveCouponUsage(couponUsage);
        }

        responseOrder.setId(customerOrder.getId());
        confirmResponseDTO.setMessage(OndcConfirmResponseDTO.Message.builder().order(responseOrder).build());
        try {
            log.info("Response to confirm for new order is {}", om.writeValueAsString(confirmResponseDTO));
            messageProducer.sendMessage(om.writeValueAsString(confirmResponseDTO));
        } catch (JsonProcessingException e) {
            log.error("Failed to send message to rabbitmq ", e);
        }

    }

    private OndcContextDTO getResponseContext(OndcContextDTO context, String responseAction) {
        return OndcContextDTO
                .builder()
                .action(responseAction).bap_id(context.getBap_id()).bap_uri(context.getBap_uri())
                .bpp_id(bppId).bpp_uri(bppUri).domain(context.getDomain())
                .version(context.getCore_version()).message_id(context.getMessage_id())
                .timestamp(LocalDateTime.now().format(formatter))
                .location(OndcContextDTO.ContextLocation.builder()
                        .country(OndcContextDTO.Country.builder().code("IND").build())
                        .city(OndcContextDTO.City.builder().code("std:080").build()).build())
                .transaction_id(context.getTransaction_id())
                .ttl("PT30S")
                .build();
    }

    private void sendErrorResponse(OndcBaseResponseDTO responseDTO, String errorCode, String errorMessage) {
        responseDTO.setError(OndcCommonDTO.OndcError.builder()
                .type("CORE-ERROR").code(errorCode).message(errorMessage)
                .build());
        try {
            log.info("Response to init is {}", om.writeValueAsString(responseDTO));
            messageProducer.sendMessage(om.writeValueAsString(responseDTO));
        } catch (JsonProcessingException e) {
            log.error("Failed to send message to rabbitmq ", e);
        }
    }
    private Pair<BigDecimal, CouponUsage> getDiscountFromCoupon(String couponId, BigDecimal totalAmount,
                                                                OndcBaseResponseDTO responseDTO) {
        CouponUsage couponUsage = couponService.getCouponUsageByUserCouponId(couponId);
        BigDecimal discount = BigDecimal.ZERO;
        if (couponUsage == null) {
            log.info("Coupon {} does not exist", couponId);
            sendErrorResponse(responseDTO, "30006", "This coupon does not exist.");
            return null;
        }
        if (couponUsage.getOrderId() != null) {
            log.info("Coupon already used for order {}", couponUsage.getOrderId());
            sendErrorResponse(responseDTO, "30006", "This coupon has already been used.");
            return null;
        }
        Coupon couponToUse = couponService.getByCouponId(couponUsage.getCouponId());
        if (couponToUse == null) {
            log.info("Coupon {} does not exist", couponId);
            sendErrorResponse(responseDTO, "30006", "This coupon does not exist.");
            return null;
        }
        if (couponToUse.getTime().getRange().getEnd().before(Date.from(Instant.now()))) {
            log.info("Coupon {} has expired", couponId);
            sendErrorResponse(responseDTO, "30006", "This coupon has expired.");
            return null;
        }
        log.info("Coupon {} is valid", couponId);
        for (OndcCommonDTO.Tag tag : couponToUse.getTags()) {
            if (tag.getCode().equals("qualifier")) {
                BigDecimal minimumAmount = new BigDecimal(tag.getList().get(0).getValue());
                if (totalAmount.compareTo(minimumAmount) < 0) {
                    log.info("Coupon {} does not qualify for this order", couponId);
                    sendErrorResponse(responseDTO, "30006", "This coupon does not qualify for this order, " +
                            "minimum amount is ." + minimumAmount);
                    return null;
                }
            } else if (tag.getCode().equals("benefit")) {
                Map<String, String> benefitMap = tag.getList().stream()
                        .collect(Collectors.toMap(
                                OndcCommonDTO.OndcList::getCode,
                                OndcCommonDTO.OndcList::getValue)
                        );
                if (benefitMap.get("value_type").equals("amount")) {
                    discount = new BigDecimal(benefitMap.get("value")).abs();
                    log.info("Flat Discount from coupon {} is {}", couponId, discount);
                } else if (benefitMap.get("value_type").equals("percent")) {
                    discount = (totalAmount.multiply(new BigDecimal(benefitMap.get("value"))))
                            .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                    discount = discount.min(new BigDecimal(benefitMap.get("value_cap")).abs());
                    log.info("Discount from perc coupon {} is {}", couponId, discount);
                }
            }
        }
        log.info("Final Discount from coupon {}", discount);
        return Pair.of(discount, couponUsage);
    }
}
