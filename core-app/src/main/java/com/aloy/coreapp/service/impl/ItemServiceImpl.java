package com.aloy.coreapp.service.impl;

import com.aloy.coreapp.dto.OndcCommonDTO;
import com.aloy.coreapp.model.Item;
import com.aloy.coreapp.repos.ItemRepository;
import com.aloy.coreapp.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public void saveSellerItems(String sellerId, String bppProviderId, List<OndcCommonDTO.Item> items) {
        //For now, just delete and insert again as it is an async op
        itemRepository.deleteAllByBppProviderId(bppProviderId);
        List<Item> itemsToSave = new ArrayList<>();
        items.forEach(i -> {
            itemsToSave.add(Item.builder().bppProviderId(bppProviderId).sellerId(sellerId).images(i.getDescriptor().getImages())
                    .price(i.getPrice()).categoryId(i.getCategory_id()).fulfillmentId(i.getFulfillment_id())
                    .name(i.getDescriptor().getName()).locationId(i.getLocation_id())
                    .shortDescription(i.getDescriptor().getShort_desc()).longDescription(i.getDescriptor().getLong_desc())
                    .bppItemId(i.getId()).createdAt(Date.from(Instant.now())).isActive(true).build());
        });
        itemRepository.saveAll(itemsToSave);
    }

    @Override
    public List<Item> getActiveItemsForSeller(String sellerId) {
        return itemRepository.findAllBySellerIdAndIsActiveIsTrue(sellerId);
    }

    @Override
    public List<Item> getItemsByIds(List<String> itemIds) {
        return itemRepository.findAllByIdIn(itemIds);
    }

}
