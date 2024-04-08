package com.aloy.sellerbppservice.service;

import com.aloy.sellerbppservice.model.Provider;

import java.util.List;

public interface ProviderService {

    Provider add(Provider provider);

    List<Provider> findActiveProviders();

    Provider getById(String id);
}
