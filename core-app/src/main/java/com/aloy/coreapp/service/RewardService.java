package com.aloy.coreapp.service;

import com.aloy.coreapp.dto.BadgeDTO;
import com.aloy.coreapp.dto.CouponDTO;
import com.aloy.coreapp.dto.RetroRewardDTO;
import com.aloy.coreapp.dto.UserCouponDTO;
import com.aloy.coreapp.dto.nft.BadgeNftRequestDTO;
import com.aloy.coreapp.dto.nft.NftResponseDTO;
import com.aloy.coreapp.enums.BadgeType;
import com.aloy.coreapp.model.Badge;
import com.aloy.coreapp.model.Coupon;
import com.aloy.coreapp.model.RetroReward;
import com.aloy.coreapp.model.UserCoupon;

import java.util.List;

public interface RewardService {

    Coupon addCoupon(Coupon coupons);

    List<CouponDTO> getAvailableCoupons();

    List<UserCouponDTO> getUserCoupons(String userId);

    Boolean purchaseCoupon(String userId, String couponId);

    List<UserCouponDTO> getApplicableCoupons(String userId, String sellerId, String orderAmount);

    UserCoupon getUserCoupon(String id);

    void markCouponUsage(String sellerCouponId, String orderId);

    void addPoints(String userId, int points);

    RetroReward addRetroReward(RetroRewardDTO retroRewardDTO);

    void addBadge(BadgeDTO badgeDTO, NftResponseDTO nftData);

    List<Badge> getUserBadges(String userId);
}
