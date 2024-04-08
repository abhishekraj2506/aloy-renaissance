package com.aloy.coreapp.dto.rabbit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileNftMessageDTO {
    private String userId;
    private String walletAddress;
    private String nftAddress;
}
