package com.aloy.coreapp.repos;

import com.aloy.coreapp.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {

    List<Item> findAllByBppProviderIdAndIsActiveIsTrue(String bppProviderId);

    List<Item> findAllBySellerIdAndIsActiveIsTrue(String sellerId);

    void deleteAllByBppProviderId(String bppProviderId);
    List<Item> findAllByIdIn(List<String> itemIds);
}
