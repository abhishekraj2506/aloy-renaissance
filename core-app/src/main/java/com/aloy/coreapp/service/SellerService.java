package com.aloy.coreapp.service;

import com.aloy.coreapp.dto.OndcCommonDTO;
import com.aloy.coreapp.model.Seller;

import java.util.List;

public interface SellerService {

    Seller addSellerFromOndcSearch(OndcCommonDTO.Provider provider, String bppId, String bppUri);

    Seller getSellerByAloyId(String sellerId);
    List<Seller> getActiveSellers();
}
