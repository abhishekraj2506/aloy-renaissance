package com.aloy.coreapp.repos;

import com.aloy.coreapp.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CouponRepository extends MongoRepository<Coupon, String> {

    List<Coupon> findAllByIsActiveIsTrue();
}
