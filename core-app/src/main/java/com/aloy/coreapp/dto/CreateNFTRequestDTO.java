package com.aloy.coreapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNFTRequestDTO {
    private String name;
    private String image;
    private String receiverAddress;
    private Map<String, String> attributes;
}
