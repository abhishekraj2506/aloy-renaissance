package com.aloy.coreapp.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
@Builder
public class UserCoupon {
    @Id
    private String id;
    private String userId;
    private String couponId;
    private String sellerId;
    private String sellerCouponId;
    private boolean isUsed = false;
    private String orderId;
    private Date usedAt;
    private Date createdAt;
    private Date expiresAt;
}
