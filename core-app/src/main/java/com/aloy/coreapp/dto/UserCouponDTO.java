package com.aloy.coreapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class UserCouponDTO {
    private String id;
    private String couponId;
    private String couponDescription;
    private String couponSubDescription;
    private String couponDisplayCode;
    private String sellerName;
    private String sellerLogoUrl;
    private Long expiresAt;
    private Boolean isUsed;
    private Boolean isExpired;
    private Boolean isApplicable;
    private BigDecimal minimumOrderAmount;
}
