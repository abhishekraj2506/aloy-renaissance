package com.aloy.coreapp.dto.rabbit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWalletRabbitMessageDTO {
    private String userId;
    private String googleIdToken;
    private boolean mintNft;
}
