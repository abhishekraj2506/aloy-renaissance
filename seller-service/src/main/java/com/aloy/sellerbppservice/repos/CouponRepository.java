package com.aloy.sellerbppservice.repos;

import com.aloy.sellerbppservice.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CouponRepository extends MongoRepository<Coupon, String> {
}
