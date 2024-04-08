package com.aloy.coreapp.model;

import com.aloy.coreapp.dto.OndcCommonDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;

@Data
@Document
public class Seller {

    @Id
    private String id;

    private String bppProviderId;
    private String bppId;
    private String bppUri;
    private String name;
    private OndcCommonDTO.Descriptor descriptor;
    private List<OndcCommonDTO.Location> locations;
    private List<OndcCommonDTO.Category> categories;
    private List<OndcCommonDTO.Fulfillment> fulfillments;
    private String logoUrl;
    private BigDecimal rating;
    private boolean isActive = true;

}
