package com.aloy.sellerbppservice.model;


import com.aloy.sellerbppservice.dto.OndcCommonDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Document
public class Coupon {
    @Id
    private String id;

    private String providerId;

    public OndcCommonDTO.Descriptor descriptor;
    @Field("location_ids")
    public List<String> locationIds;
    @Field("item_ids")
    public List<String> itemIds;
    public OndcCommonDTO.Time time;
    public List<OndcCommonDTO.Tag> tags;
}

