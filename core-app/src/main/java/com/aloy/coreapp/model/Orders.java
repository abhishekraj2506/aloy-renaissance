package com.aloy.coreapp.model;

import com.aloy.coreapp.dto.OndcCommonDTO;
import com.aloy.coreapp.enums.OrderStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document
public class Orders {
    @Id
    private String id;

    private String userId;
    private String sellerOrderId;
    private OndcCommonDTO.Provider provider;
    private List<OndcCommonDTO.Item> items;
    private List<OndcCommonDTO.Fulfillment> fulfillments;
    private OndcCommonDTO.Quote quote;
    private OndcCommonDTO.Billing billing;
    private List<OndcCommonDTO.Payment> payments;
    private List<OndcCommonDTO.CancellationTerm> cancellationTerms;
    private int points;
    private OrderStatus orderStatus;
    private String userCouponId;
}
