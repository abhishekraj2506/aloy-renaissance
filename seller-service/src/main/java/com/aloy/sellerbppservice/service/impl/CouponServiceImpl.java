package com.aloy.sellerbppservice.service.impl;

import com.aloy.sellerbppservice.model.Coupon;
import com.aloy.sellerbppservice.model.CouponUsage;
import com.aloy.sellerbppservice.repos.CouponRepository;
import com.aloy.sellerbppservice.repos.CouponUsageRepository;
import com.aloy.sellerbppservice.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;


    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Override
    public Coupon create(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public List<String> addCouponsToUsage(String parentCouponId, int numberOfCoupons) {
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < numberOfCoupons; i++) {
            CouponUsage couponUsage = new CouponUsage();
            couponUsage.setCouponId(parentCouponId);
            String uuid = UUID.randomUUID().toString();
            couponUsage.setUserCouponId(uuid);
            uuids.add(uuid);
            couponUsageRepository.save(couponUsage);
        }
        return uuids;
    }

    @Override
    public CouponUsage getCouponUsageByUserCouponId(String couponId) {
        return couponUsageRepository.findByUserCouponId(couponId).orElse(null);
    }

    @Override
    public Coupon getByCouponId(String couponId) {
        return couponRepository.findById(couponId).orElse(null);
    }

    @Override
    public void saveCouponUsage(CouponUsage couponUsage) {
        couponUsageRepository.save(couponUsage);
    }
}
