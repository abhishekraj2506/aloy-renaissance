package com.aloy.sellerbppservice.dto;

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
public class OndcConfirmResponseDTO extends OndcBaseResponseDTO {

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
        private String id;
        private OndcCommonDTO.Provider provider;
        private List<OndcCommonDTO.Item> items;
        private List<OndcCommonDTO.Offer> offers;
        private List<OndcCommonDTO.Fulfillment> fulfillments;
        private OndcCommonDTO.Quote quote;
        private OndcCommonDTO.Billing billing;
        private List<OndcCommonDTO.Payment> payments;
        private List<OndcCommonDTO.CancellationTerm> cancellation_terms;
    }

}
