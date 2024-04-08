package com.aloy.sellerbppservice.service.impl;

import com.aloy.sellerbppservice.model.Item;
import com.aloy.sellerbppservice.repos.ItemRepository;
import com.aloy.sellerbppservice.repos.ProviderRepository;
import com.aloy.sellerbppservice.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public Item addItem(Item item) {
        return itemRepository.save(item);
    }

    @Override
    public List<Item> getItemsForProvider(String providerId) {
        return itemRepository.findAllByProviderIdAndIsActiveIsTrue(providerId);
    }

    @Override
    public Item getById(String id) {
        return itemRepository.findById(id).orElse(null);
    }
}
