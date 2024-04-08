package com.aloy.sellerbppservice.model;

import com.aloy.sellerbppservice.dto.OndcCommonDTO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "orders")
public class Orders {
    @Id
    private String id;

    private String ondcTransactionId;
    private OndcCommonDTO.Provider provider;
    private List<OndcCommonDTO.Item> items;
    private List<OndcCommonDTO.Fulfillment> fulfillments;
    private OndcCommonDTO.Billing billing;
    private List<OndcCommonDTO.Payment> payments;
    private OndcCommonDTO.Quote quote;
    private List<OndcCommonDTO.CancellationTerm> cancellation_terms;

    private String couponUsageId;
}
