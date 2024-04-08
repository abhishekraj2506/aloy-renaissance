package com.aloy.coreapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    private String id;
    private String code;
    private String description;
    private String subDescription;
    private String sellerName;
    private String sellerLogoUrl;
    private int points;
    private long expiresAt;
    private boolean isUsed;
    private boolean isExpired;
}
