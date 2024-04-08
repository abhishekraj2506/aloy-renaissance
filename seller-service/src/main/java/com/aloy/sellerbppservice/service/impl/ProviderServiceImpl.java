package com.aloy.sellerbppservice.service.impl;

import com.aloy.sellerbppservice.model.Provider;
import com.aloy.sellerbppservice.repos.ProviderRepository;
import com.aloy.sellerbppservice.service.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ProviderServiceImpl implements ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    @Override
    public Provider add(Provider provider) {
        return providerRepository.save(provider);
    }

    @Override
    public List<Provider> findActiveProviders() {
        return providerRepository.findAllByIsActiveIsTrue();
    }

    @Override
    public Provider getById(String id) {
        return providerRepository.findById(id).orElse(null);
    }
}
