package com.aloy.coreapp.model;

import com.aloy.coreapp.dto.OndcCommonDTO;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document
@Builder
public class Item {

    @Id
    private String id;

    private String sellerId;
    private String bppProviderId;
    private String bppItemId;

    private String categoryId;
    private String fulfillmentId;
    private String locationId;

    private List<OndcCommonDTO.Image> images;
    private String name;

    private String shortDescription;

    private String longDescription;

    private OndcCommonDTO.Price price;
    private boolean isActive = true;

    private Date createdAt;
}
