package com.aloy.coreapp.dto.okto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletDTO {
    private String client;
    private String accessToken;
    private String refreshAuthToken;
    private String deviceToken;
    private String walletAddress;
}
