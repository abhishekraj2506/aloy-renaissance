package com.aloy.coreapp.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OndcSearchRequestDTO {

    private OndcContextDTO context;
    private Message message;

    @Data
    @Builder
    public static class Message {
        private OndcCommonDTO.Intent intent;
    }
}
