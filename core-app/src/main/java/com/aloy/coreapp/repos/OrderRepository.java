package com.aloy.coreapp.repos;

import com.aloy.coreapp.model.Orders;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Orders, String> {

    Optional<Orders> findBySellerOrderId(String sellerOrderId);
    List<Orders> findAllByUserId(String userId);
}
