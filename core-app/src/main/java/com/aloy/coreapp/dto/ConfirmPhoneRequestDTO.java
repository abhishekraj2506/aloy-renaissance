package com.aloy.coreapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPhoneRequestDTO {
    private String phoneNumber;
    private String otp;
}
