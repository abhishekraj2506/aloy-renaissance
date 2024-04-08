package com.aloy.coreapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiateOrderRequestDTO {
    private String sellerId;
    private List<CartItem> items;
    private String userAddressId;
    private String couponId;

    @Data
    public static class CartItem {
        private String itemId;
        private int quantity;
    }

}
