package com.aloy.coreapp.dto.okto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OktoSetPinRequestDTO {
    @JsonProperty("id_token")
    private String idToken;
    private String token;

    @JsonProperty("relogin_pin")
    private String relogin_pin;
    private String purpose;

}
