package com.aloy.sellerbppservice.repos;

import com.aloy.sellerbppservice.model.CouponUsage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CouponUsageRepository extends MongoRepository<CouponUsage, String> {

    Optional<CouponUsage> findByCouponId(String couponId);

    Optional<CouponUsage> findByUserCouponId(String userCouponId);
}
