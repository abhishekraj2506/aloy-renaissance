package com.aloy.sellerbppservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class Item {
    @Id
    private String id;

    private String providerId;
    private String categoryId;
    private String fulfillmentId;
    private String locationId;

    private List<ItemImage> images;
    private String name;

    private String shortDescription;

    private String longDescription;

    private ItemPrice price;
    private boolean isActive = true;

    @Data
    public static final class ItemPrice {
        private String listedValue;
        private String currency;
        private String value;
    }

    @Data
    public static final class ItemImage {
        private String url;
    }
}
