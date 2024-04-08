package com.aloy.coreapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Document
@Data
public class Coupon {

    @Id
    private String id;
    private String displayCode;
    private String sellerLogoUrl;
    private String sellerId;
    private String sellerName;
    private String description;
    private String subDescription;
    private int points;
    private Date expiresAt;
    private BigDecimal minimumOrderAmount;
    private boolean isActive = true;
    private Set<String> availableCouponIds;
}
