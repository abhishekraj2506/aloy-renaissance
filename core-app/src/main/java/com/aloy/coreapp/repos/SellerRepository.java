package com.aloy.coreapp.repos;

import com.aloy.coreapp.model.Seller;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends MongoRepository<Seller, String> {

    Optional<Seller> findByBppProviderId(String bppProviderId);

    List<Seller> findAllByIsActiveIsTrue();
}
