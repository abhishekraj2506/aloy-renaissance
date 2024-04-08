package com.aloy.coreapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfirmOrderRequestDTO {
    private String sellerId;
    private List<InitiateOrderRequestDTO.CartItem> items;
    private String userAddressId;
    private BigDecimal totalAmount;
    private String ondcTransactionId;
    private String couponId;
}
