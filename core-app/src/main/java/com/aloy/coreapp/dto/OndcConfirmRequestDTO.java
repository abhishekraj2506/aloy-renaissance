package com.aloy.coreapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OndcConfirmRequestDTO {

    public OndcContextDTO context;
    public Message message;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        public Order order;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Order {
        public OndcCommonDTO.Provider provider;
        public List<OndcCommonDTO.Item> items;
        public List<OndcCommonDTO.Fulfillment> fulfillments;
        public List<OndcCommonDTO.Offer> offers;
        public OndcCommonDTO.Billing billing;
        public List<OndcCommonDTO.Payment> payments;
    }

}
