package com.aloy.coreapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserLoginResponseDTO {
    private boolean isNewUser;
    private String accessToken;
    private String wsToken;
    private String name;
    private String email;
}
