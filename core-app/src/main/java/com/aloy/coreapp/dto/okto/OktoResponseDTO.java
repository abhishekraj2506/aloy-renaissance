package com.aloy.coreapp.dto.okto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OktoResponseDTO {

    public String status;
    public Data data;
    public OktoError error;


    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OktoError {
        public String code;
        public String errorCode;
        public Boolean message;
    }

    @Builder
    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        public String token;
        public String message;
        public int status;
        public String action;
        public int code;


        @JsonProperty("auth_token")
        public String authToken;
        @JsonProperty("refresh_auth_token")
        public String refreshAuthToken;
        @JsonProperty("device_token")
        public String deviceToken;

        private List<Wallet> wallets;

        @lombok.Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Wallet {
            @JsonProperty("network_name")
            public String networkName;
            public String address;
            public Boolean success;
        }
    }
}
