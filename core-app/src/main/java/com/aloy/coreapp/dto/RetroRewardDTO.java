package com.aloy.coreapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetroRewardDTO {
    private String source;
    private String sourceData;
    private String userId;
//    private BigDecimal points;
}
