package com.aloy.coreapp.service;

import com.aloy.coreapp.client.NftServiceClient;
import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.dto.nft.NftResponseDTO;
import com.aloy.coreapp.dto.nft.ProfileNftRequestDTO;
import com.aloy.coreapp.dto.okto.WalletDTO;
import com.aloy.coreapp.dto.rabbit.CreateWalletRabbitMessageDTO;
import com.aloy.coreapp.dto.rabbit.UpdateProfileNftMessageDTO;
import com.aloy.coreapp.enums.BadgeType;
import com.aloy.coreapp.handler.CustomerWebSocketHandler;
import com.aloy.coreapp.model.Orders;
import com.aloy.coreapp.model.Seller;
import com.aloy.coreapp.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RequestHandlerService {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerWebSocketHandler customerWebSocketHandler;

    @Autowired
    private LoyaltyEngineService loyaltyEngineService;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private Web3WalletService web3WalletService;

    @Autowired
    private NftServiceClient nftServiceClient;

    public void handleSearchResponse(OndcSearchResponseDTO searchResponseDTO) {
        log.info("Handling search response");
        OndcContextDTO context = searchResponseDTO.getContext();
        OndcSearchResponseDTO.Message message = searchResponseDTO.getMessage();
        message.getCatalog().getProviders().forEach(p -> {
            log.info("Saving details for provider {}", p.getId());
            Seller seller = sellerService.addSellerFromOndcSearch(p, context.getBpp_id(), context.getBpp_uri());
            log.info("Saving items for provider {}", p.getId());
            itemService.saveSellerItems(seller.getId(), p.getId(), p.getItems());
        });
    }

    public void handleInitResponse(OndcInitResponseDTO initResponseDTO) {
        log.info("Handling init response");
        String phone = initResponseDTO.getMessage().getOrder().getBilling().getPhone();
        User user = userService.getByPhoneNumber(phone);
        SocketMessageDTO<OndcInitResponseDTO.Order> socketMessage = SocketMessageDTO.<OndcInitResponseDTO.Order>builder()
                .message(initResponseDTO.getMessage().getOrder())
                .type("init")
                .build();
        log.info("Consumed init request from queue for user {} {}", user.getId(), user.getName());
        try {
            customerWebSocketHandler.sendMessageToConnection("WS-".concat(user.getId()),
                    om.writeValueAsString(socketMessage));
        } catch (Exception e) {
            log.error("Failed to handle init response from queue");
        }

    }

    public void handleConfirmResponse(OndcConfirmResponseDTO confirmResponseDTO) {
        log.info("Handling confirm response");
        String phone = confirmResponseDTO.getMessage().getOrder().getBilling().getPhone();
        User user = userService.getByPhoneNumber(phone);
        log.info("Consumed confirm request from queue for user {} {}", user.getId(), user.getName());
        OndcConfirmResponseDTO.Order ondcOrder = confirmResponseDTO.getMessage().getOrder();
        BigDecimal actualOrderAmount = new BigDecimal(ondcOrder.getQuote().getPrice().getValue());
        BigInteger points = loyaltyEngineService.getPoints(actualOrderAmount);
        log.info("User {} has earned {} points for this order of amount {}", user.getId(), points, actualOrderAmount);
        SavedOrderDTO savedOrderDTO = orderService.saveOrder(ondcOrder, user.getId(), points.intValue());
        Orders savedOrder = savedOrderDTO.getOrder();
        if (savedOrderDTO.isNewOrder()) {
            //Update user points
            rewardService.addPoints(user.getId(), points.intValue());
        }
        if (ondcOrder.getOffers() != null && !ondcOrder.getOffers().isEmpty()) {
            OndcCommonDTO.Offer offer = ondcOrder.getOffers().get(0);
            rewardService.markCouponUsage(offer.getId(), savedOrder.getId());
        }
        //Trim ondc order for response
        ondcOrder.setId(savedOrder.getId());
        ondcOrder.setProvider(null);
        ondcOrder.setItems(null);
        SocketMessageDTO<OndcConfirmResponseDTO.Order> socketMessage = SocketMessageDTO.<OndcConfirmResponseDTO.Order>builder()
                .message(ondcOrder)
                .type("confirm")
                .build();
        socketMessage.setParams(Map.of("points", String.valueOf(savedOrder.getPoints())));
        try {
            customerWebSocketHandler.sendMessageToConnection("WS-".concat(user.getId()),
                    om.writeValueAsString(socketMessage));
        } catch (Exception e) {
            log.error("Failed to handle init response from queue");
        }
        loyaltyEngineService.checkIfUserEligibleForBadge(user.getId());
    }

    public void handleCreateWalletTask(RabbitTaskMessageDTO<CreateWalletRabbitMessageDTO> messageDTO) {
        CreateWalletRabbitMessageDTO data = messageDTO.getData();
        String userId = data.getUserId();
        User user = userService.getById(userId);
        if (user.getWallet() == null) {
            log.info("Handling create wallet task for user {}", userId);
            WalletDTO wallet = web3WalletService.createWallet(userId, data.getGoogleIdToken());
            userService.addWallet(userId, wallet);
            log.info("Created wallet for user {} with address {}", userId, wallet.getWalletAddress());
            NftResponseDTO nftResponseDTO = nftServiceClient.createOrUpdateProfileNft(
                    ProfileNftRequestDTO.builder()
                            .badgeData(
                            ProfileNftRequestDTO.BadgeData.builder()
                                    .attributes(List.of(
                                            ProfileNftRequestDTO.Attribute.builder().traitType("currentPoints").value("0").build(),
                                            ProfileNftRequestDTO.Attribute.builder().traitType("lifetimePoints").value("0").build()))
                                    .build()
                            )
                            .receiverAddress(wallet.getWalletAddress())
                    .build());
            nftResponseDTO.setReceiverWalletAddress(wallet.getWalletAddress());
            log.info("Created profile nft for user {} {}", userId, nftResponseDTO);
            rewardService.addBadge(BadgeDTO.builder().badgeType(BadgeType.ALOY_MEMBER).userId(userId).build(), nftResponseDTO);
        }
    }

    public void handleUpdateProfileNftTask(RabbitTaskMessageDTO<UpdateProfileNftMessageDTO> messageDTO) {
        UpdateProfileNftMessageDTO data = messageDTO.getData();
        String userId = data.getUserId();
        User user = userService.getById(userId);
        NftResponseDTO nftResponseDTO = nftServiceClient.createOrUpdateProfileNft(
                ProfileNftRequestDTO.builder()
                        .badgeData(
                                ProfileNftRequestDTO.BadgeData.builder()
                                        .attributes(List.of(
                                                ProfileNftRequestDTO.Attribute.builder().traitType("currentPoints")
                                                        .value(String.valueOf(user.getAvailablePoints())).build(),
                                                ProfileNftRequestDTO.Attribute.builder().traitType("lifetimePoints")
                                                        .value(String.valueOf(user.getLifetimePoints())).build()))
                                        .nftAddress(data.getNftAddress())
                                        .build()
                        )
                        .build());
        log.info("Updated profile nft for user {} {}", userId, nftResponseDTO);
    }
}
