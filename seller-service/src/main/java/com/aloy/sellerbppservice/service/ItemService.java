package com.aloy.sellerbppservice.service;

import com.aloy.sellerbppservice.model.Item;

import java.util.List;

public interface ItemService {

    Item addItem(Item item);

    List<Item> getItemsForProvider(String providerId);

    Item getById(String id);
}
