package com.aloy.coreapp.service.impl;

import com.aloy.coreapp.dto.OndcCommonDTO;
import com.aloy.coreapp.exception.CoreServiceException;
import com.aloy.coreapp.model.Seller;
import com.aloy.coreapp.repos.SellerRepository;
import com.aloy.coreapp.service.SellerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    public Seller addSellerFromOndcSearch(OndcCommonDTO.Provider provider, String bppId, String bppUri) {
        Optional<Seller> sellerOptional = sellerRepository.findByBppProviderId(provider.getId());
        if (sellerOptional.isEmpty()) {
            Seller seller = new Seller();
            seller.setBppProviderId(provider.getId());
            seller.setName(provider.getDescriptor().getName());
            seller.setLocations(provider.getLocations());
            seller.setCategories(provider.getCategories());
            seller.setFulfillments(provider.getFulfillments());
            seller.setDescriptor(provider.getDescriptor());
            seller.setBppId(bppId);
            seller.setBppUri(bppUri);
            seller.setActive(true);
            return sellerRepository.save(seller);
        }
        return sellerOptional.get();
    }

    @Override
    public Seller getSellerByAloyId(String sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new CoreServiceException("Seller not found"));
    }

    @Override
    public List<Seller> getActiveSellers() {
        return sellerRepository.findAllByIsActiveIsTrue();
    }
}
