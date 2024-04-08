package com.aloy.sellerbppservice.repos;

import com.aloy.sellerbppservice.model.Provider;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProviderRepository extends MongoRepository<Provider, String> {

    List<Provider> findAllByIsActiveIsTrue();
}
