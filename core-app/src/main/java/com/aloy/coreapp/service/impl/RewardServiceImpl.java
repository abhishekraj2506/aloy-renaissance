package com.aloy.coreapp.service.impl;

import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.dto.nft.NftResponseDTO;
import com.aloy.coreapp.dto.rabbit.UpdateProfileNftMessageDTO;
import com.aloy.coreapp.enums.BadgeType;
import com.aloy.coreapp.enums.TaskType;
import com.aloy.coreapp.exception.CoreServiceException;
import com.aloy.coreapp.messaging.RabbitMessageProducer;
import com.aloy.coreapp.model.*;
import com.aloy.coreapp.repos.BadgeRepository;
import com.aloy.coreapp.repos.CouponRepository;
import com.aloy.coreapp.repos.RetroRewardRepository;
import com.aloy.coreapp.repos.UserCouponRepository;
import com.aloy.coreapp.service.RewardService;
import com.aloy.coreapp.service.SellerService;
import com.aloy.coreapp.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class RewardServiceImpl implements RewardService {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private RetroRewardRepository retroRewardRepository;

    @Autowired
    private RabbitMessageProducer rabbitMessageProducer;

    @Autowired
    private ObjectMapper om;

    @Value("${profile.nft.image.uri}")
    private String profileNftImage;

    @Value("${explorer.nft.image.uri}")
    private String explorerNftImage;

    @Override
    public Coupon addCoupon(Coupon coupon) {
        Seller sellerByAloyId = sellerService.getSellerByAloyId(coupon.getSellerId());
        coupon.setSellerName(sellerByAloyId.getName());
        coupon.setSellerLogoUrl(sellerByAloyId.getDescriptor().getImages().get(0).getUrl());
        return couponRepository.save(coupon);
    }

    @Override
    public List<CouponDTO> getAvailableCoupons() {
        List<Coupon> coupons = couponRepository.findAllByIsActiveIsTrue();
        return coupons.stream().filter(coupon -> !coupon.getAvailableCouponIds().isEmpty())
                .map(RewardServiceImpl::getCouponDTO).toList();
    }

    private static CouponDTO getCouponDTO(Coupon coupon) {
        return CouponDTO.builder().id(coupon.getId()).description(coupon.getDescription())
                .subDescription(coupon.getSubDescription()).code(coupon.getDisplayCode())
                .sellerName(coupon.getSellerName()).expiresAt(coupon.getExpiresAt().getTime())
                .sellerLogoUrl(coupon.getSellerLogoUrl())
                .points(coupon.getPoints()).build();
    }

    @Override
    public List<UserCouponDTO> getUserCoupons(String userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (userCoupons.isEmpty()) {
            return List.of();
        }
        return userCoupons.stream().map(userCoupon -> {
            Optional<Coupon> couponOptional = couponRepository.findById(userCoupon.getCouponId());
            if (couponOptional.isPresent()) {
                Coupon coupon = couponOptional.get();
                return toUserCouponDTO(userCoupon, coupon, null, null);
            } else {
                return null;
            }
        }).collect(toList());
    }

    @Override
    public Boolean purchaseCoupon(String userId, String couponId) {
        User user = userService.getById(userId);
        Optional<Coupon> couponOptional = couponRepository.findById(couponId);
        if (couponOptional.isEmpty()) {
            throw new CoreServiceException("Coupon not found");
        }
        Coupon coupon = couponOptional.get();
        if (user.getAvailablePoints() < coupon.getPoints()) {
            throw new CoreServiceException("Insufficient points");
        }
        String couponForUser = coupon.getAvailableCouponIds().stream()
                .findFirst().orElseThrow(() -> new CoreServiceException("Coupon sold out."));
        coupon.getAvailableCouponIds().remove(couponForUser);

        UserCoupon userCoupon = UserCoupon.builder()
                .couponId(couponId).userId(userId).createdAt(Date.from(Instant.now()))
                .sellerId(coupon.getSellerId())
                .sellerCouponId(couponForUser)
                .expiresAt(coupon.getExpiresAt())
                .isUsed(false).build();
        addPoints(userId, coupon.getPoints() * (-1));
        userCouponRepository.save(userCoupon);
        couponRepository.save(coupon);
        return true;
    }


    @Override
    public void addPoints(String userId, int points) {
        userService.addPoints(userId, points);
        Optional<Badge> profileBadgeOptional = badgeRepository.findByUserIdAndBadgeType(userId, BadgeType.ALOY_MEMBER);
        if (profileBadgeOptional.isEmpty()) {
            log.info("Cannot update profile nft for user {} as profile badge is not present", userId);
            return;
        }
        Badge badge = profileBadgeOptional.get();
        RabbitTaskMessageDTO<UpdateProfileNftMessageDTO> messageDTO = new RabbitTaskMessageDTO<>();
        messageDTO.setType(TaskType.UPDATE_PROFILE_NFT);
        messageDTO.setData(UpdateProfileNftMessageDTO.builder()
                .userId(userId).nftAddress(badge.getNftData().getNftAddress()).build());
        try {
            rabbitMessageProducer.sendMessage(om.writeValueAsString(messageDTO));
        } catch (JsonProcessingException e) {
            log.info("Failed to send profile update message to rabbitmq");
        }
    }

    @Override
    public List<UserCouponDTO> getApplicableCoupons(String userId, String sellerId, String orderAmount) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserIdAndSellerIdAndIsUsedIsFalse(userId, sellerId);
        List<UserCouponDTO> applicableCoupons = new ArrayList<>();
        if (userCoupons.isEmpty()) {
            return applicableCoupons;
        }

        for (UserCoupon userCoupon : userCoupons) {
            Optional<Coupon> couponOptional = couponRepository.findById(userCoupon.getCouponId());
            if (couponOptional.isPresent()) {
                Coupon coupon = couponOptional.get();
                if (coupon.getExpiresAt().before(Date.from(Instant.now()))) {
                    continue;
                }
                if (coupon.getMinimumOrderAmount().compareTo(new BigDecimal(orderAmount)) > 0) {
                    applicableCoupons.add(toUserCouponDTO(userCoupon, coupon, false, false));
                    continue;
                }
                applicableCoupons.add(toUserCouponDTO(userCoupon, coupon, false, true));
            }
        }
        return applicableCoupons;
    }

    @Override
    public UserCoupon getUserCoupon(String id) {
        return userCouponRepository.findById(id).orElseThrow(() -> new CoreServiceException("Coupon not found"));
    }

    @Override
    public void markCouponUsage(String sellerCouponId, String orderId) {
        //User id also
        Optional<UserCoupon> userCouponOptional = userCouponRepository.findBySellerCouponId(sellerCouponId);
        if (userCouponOptional.isEmpty()) {
            return;
        }
        UserCoupon userCoupon = userCouponOptional.get();
        userCoupon.setUsedAt(Date.from(Instant.now()));
        userCoupon.setUsed(true);
        userCoupon.setOrderId(orderId);
        userCouponRepository.save(userCoupon);
    }

    @Override
    public RetroReward addRetroReward(RetroRewardDTO retroRewardDTO) {
        String sourceData = retroRewardDTO.getSourceData();
        if (!retroRewardDTO.getSource().equals("SWIGGY")) {
            throw new CoreServiceException("Invalid source");
        }
        Map<String, Object> swiggyOrderDetails = getSwiggyOrderDetails(sourceData);
        log.info("Swiggy order details {}", swiggyOrderDetails);
        String sourceUniqueId = swiggyOrderDetails.get("order_group_id").toString();
        Optional<RetroReward> retroRewardOptional = retroRewardRepository.findBySourceAndSourceUniqueId(retroRewardDTO.getSource(),
                sourceUniqueId);
        if (retroRewardOptional.isPresent())
            return RetroReward.builder().points(retroRewardOptional.get().getPoints()).build();
        ;
        BigDecimal orderValue = (BigDecimal) swiggyOrderDetails.get("transaction_amount");
        int points = orderValue.intValue();
        RetroReward reward = RetroReward.builder().source(retroRewardDTO.getSource())
                .data(swiggyOrderDetails)
                .sourceUniqueId(sourceUniqueId)
                .userId(retroRewardDTO.getUserId())
                .points(points).build();
        retroRewardRepository.save(reward);
        addPoints(retroRewardDTO.getUserId(), points);
        return RetroReward.builder().points(points).build();
    }

    @Override
    public void addBadge(BadgeDTO badgeDTO, NftResponseDTO nftData) {
        //only 1 aloy member badge can be there
        BadgeType badgeType = badgeDTO.getBadgeType();
        if (BadgeType.ALOY_MEMBER.equals(badgeDTO.getBadgeType())) {
            Optional<Badge> badgeOptional = badgeRepository.findByUserIdAndBadgeType(badgeDTO.getUserId(), badgeType);
            if (badgeOptional.isPresent()) {
                return;
            }
            Badge badge = Badge.builder().userId(badgeDTO.getUserId())
                    .name("Aloy")
                    .nftData(nftData)
                    .createdAt(Date.from(Instant.now()))
                    .badgeType(badgeType).description("Aloy Member Badge")
                    .imageUri(profileNftImage)
                    .build();
            badgeRepository.save(badge);
        } else if (BadgeType.EXPLORER.equals(badgeDTO.getBadgeType())) {
            Badge badge = Badge.builder().userId(badgeDTO.getUserId())
                    .name(badgeDTO.getName())
                    .description(badgeDTO.getDescription())
                    .badgeType(BadgeType.EXPLORER)
                    .nftData(nftData)
                    .createdAt(Date.from(Instant.now()))
                    .imageUri(explorerNftImage)
                    .build();
            badgeRepository.save(badge);
        }
    }

    @Override
    public List<Badge> getUserBadges(String userId) {
        return badgeRepository.findByUserId(userId);
    }

    private static UserCouponDTO toUserCouponDTO(UserCoupon userCoupon, Coupon coupon, Boolean isExpired,
                                                 Boolean isApplicable) {
        return UserCouponDTO.builder().id(userCoupon.getId()).couponId(userCoupon.getCouponId())
                .couponDescription(coupon.getDescription())
                .couponDisplayCode(coupon.getDisplayCode()).expiresAt(userCoupon.getExpiresAt().getTime())
                .expiresAt(userCoupon.getExpiresAt().getTime())
                .sellerName(coupon.getSellerName())
                .sellerLogoUrl(coupon.getSellerLogoUrl())
                .isExpired(isExpired).isApplicable(isApplicable)
                .isUsed(userCoupon.getUsedAt() != null).build();
    }

    Map<String, Object> getSwiggyOrderDetails(String sourceData) {
        log.info("Order data {}", sourceData);
        String input = sourceData.substring(25, sourceData.length() - 50);
        input = input.replace("*", "");
        input = input.replace("\"\"", "\",\"");
        System.out.println(input);
        String[] split = input.split(",");

        log.info("Details {}", Arrays.stream(split).toList());

        // Split the string into key-value pairs
        String[] pairs = input.split(",");

        // Creating a map to store key-value pairs
        Map<String, Object> resultMap = new HashMap<>();

        // Adding key-value pairs to the map
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].replaceAll("\"", "");
            String value = keyValue[1].replaceAll("\"", "");
            // Parsing values as per their types (assuming transaction_amount is always an integer)
            Object parsedValue = value.matches("\\d+") ? new BigDecimal(value) : value;
            resultMap.put(key, parsedValue);
        }
        return resultMap;
    }


}
