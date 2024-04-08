package com.aloy.coreapp.repos;

import com.aloy.coreapp.model.UserCoupon;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends MongoRepository<UserCoupon, String> {

    List<UserCoupon> findByUserIdOrderByCreatedAtDesc(String userId);
    List<UserCoupon> findByUserIdAndSellerIdAndIsUsedIsFalse(String userId, String sellerId);

    Optional<UserCoupon> findBySellerCouponId(String sellerCouponId);
}
