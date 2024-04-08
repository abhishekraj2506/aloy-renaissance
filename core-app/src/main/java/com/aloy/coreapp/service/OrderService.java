package com.aloy.coreapp.service;

import com.aloy.coreapp.dto.OndcConfirmResponseDTO;
import com.aloy.coreapp.dto.SavedOrderDTO;
import com.aloy.coreapp.model.Orders;

public interface OrderService {

    SavedOrderDTO saveOrder(OndcConfirmResponseDTO.Order ondcOrder, String userId, int points);

    int getUserOrderCount(String userId);
}
