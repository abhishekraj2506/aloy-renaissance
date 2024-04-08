package com.aloy.coreapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNFTResponseDTO {

    private String transactionId;
    private int nftId;
}
