package com.aloy.coreapp.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Document
@Builder
public class RetroReward {

    @Id
    private String id;
    private String userId;
    private String source;
    private String sourceUniqueId;
    private Map<String, Object> data;
    private Integer points;
}
