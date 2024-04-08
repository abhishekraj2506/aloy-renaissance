package com.aloy.coreapp.service;

import com.aloy.coreapp.dto.OndcCommonDTO;
import com.aloy.coreapp.model.Item;

import java.util.List;

public interface ItemService {

    void saveSellerItems(String sellerId, String bppProviderId, List<OndcCommonDTO.Item> items);

    List<Item> getActiveItemsForSeller(String sellerId);

    List<Item> getItemsByIds(List<String> itemIds);
}
