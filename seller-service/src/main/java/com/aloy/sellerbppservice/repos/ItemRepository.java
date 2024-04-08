package com.aloy.sellerbppservice.repos;

import com.aloy.sellerbppservice.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {

    List<Item> findAllByProviderIdAndIsActiveIsTrue(String providerId);
}
