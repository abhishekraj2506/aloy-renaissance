package com.aloy.sellerbppservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class CouponUsage {

    @Id
    private String id;

    private String couponId;

    @Indexed(unique = true)
    private String userCouponId;
    private String orderId;

}

