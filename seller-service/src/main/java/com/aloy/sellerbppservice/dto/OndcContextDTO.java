package com.aloy.sellerbppservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OndcContextDTO {

    private String domain;
    private ContextLocation location;
    private String action;
    private String core_version = "1.1.0";
    private String version = "1.1.0";
    private String bap_id;
    private String bap_uri;
    private String bpp_id;
    private String bpp_uri;
    private String ttl;
    private String message_id;
    private String timestamp;
    private String transaction_id;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextLocation {
        private Country country;
        private City city;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class City {
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Country {
        private String code;
    }
}
