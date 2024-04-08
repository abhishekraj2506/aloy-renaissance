package com.aloy.sellerbppservice.service;

import com.aloy.sellerbppservice.model.Coupon;
import com.aloy.sellerbppservice.model.CouponUsage;

import java.util.List;

public interface CouponService {

    Coupon create(Coupon coupon);

    List<String> addCouponsToUsage(String couponId, int numberOfCoupons);

    CouponUsage getCouponUsageByUserCouponId(String couponId);

    Coupon getByCouponId(String couponId);

    void saveCouponUsage(CouponUsage couponUsage);
}
