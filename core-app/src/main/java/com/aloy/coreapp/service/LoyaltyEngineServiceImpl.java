package com.aloy.coreapp.service;

import com.aloy.coreapp.client.NftServiceClient;
import com.aloy.coreapp.dto.BadgeDTO;
import com.aloy.coreapp.dto.nft.BadgeNftRequestDTO;
import com.aloy.coreapp.dto.nft.NftResponseDTO;
import com.aloy.coreapp.enums.BadgeType;
import com.aloy.coreapp.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
@Slf4j
public class LoyaltyEngineServiceImpl implements LoyaltyEngineService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private NftServiceClient nftServiceClient;

    @Autowired
    private UserService userService;

    @Autowired
    private RewardService rewardService;

    @Override
    public BigInteger getPoints(BigDecimal orderAmount) {
        //10% of order amount
        return orderAmount.multiply(BigDecimal.valueOf(0.1)).toBigInteger();
    }

    @Override
    public void checkIfUserEligibleForBadge(String userId) {
        log.info("Checking if user {} is eligible for badge", userId);
        int userOrderCount = orderService.getUserOrderCount(userId);
        if (userOrderCount != 1) {
            log.info("Not the first order, user {} is not eligible for Explorer badge", userId);
            return;
        }
        log.info("User {} is eligible for Explorer badge", userId);
        User user = userService.getById(userId);
        BadgeNftRequestDTO.BadgeData badgeData = BadgeNftRequestDTO.BadgeData.builder()
                .name("Aloy Explorer")
                .description("Awarded to users who have placed their first order")
                .imageFile("explorer.png")
                .attributes(List.of(BadgeNftRequestDTO.Attribute.builder()
                        .traitType("Level")
                        .value("1")
                        .build()))
                .build();
        NftResponseDTO badgeNft = nftServiceClient.createBadgeNft(BadgeNftRequestDTO.builder()
                .receiverAddress(user.getWallet().getWalletAddress())
                .badgeData(badgeData)
                .build());

        rewardService.addBadge(
                BadgeDTO.builder().badgeType(BadgeType.EXPLORER).userId(userId)
                        .name(badgeData.getName()).description(badgeData.getDescription())
                        .build(), badgeNft);
    }
}
