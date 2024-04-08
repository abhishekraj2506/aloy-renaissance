package com.aloy.coreapp.service;

import com.aloy.coreapp.dto.*;

import java.util.List;

public interface ShopService {

    List<SellerDTO> getSellers();

    List<SellerItemDTO> getSellerItems(String sellerId);

    InitiateOrderResponseDTO initiateOrder(InitiateOrderRequestDTO initiateOrderRequestDTO);

    ConfirmOrderResponseDTO confirmOrder(ConfirmOrderRequestDTO confirmOrderRequestDTO);

    boolean syncCatalog();
}
