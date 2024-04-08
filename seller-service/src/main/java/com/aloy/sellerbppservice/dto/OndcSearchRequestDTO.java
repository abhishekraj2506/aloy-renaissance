package com.aloy.sellerbppservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OndcSearchRequestDTO {

    private OndcContextDTO context;
    private Message message;

    @Data
    @Getter
    public static class Message {
        private OndcCommonDTO.Intent intent;
    }
}
