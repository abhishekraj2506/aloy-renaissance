package com.aloy.coreapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OndcSearchResponseDTO {

    private OndcContextDTO context;
    private Message message;

    @Data
    public static class Message {
        private OndcCommonDTO.Catalog catalog;
    }

}
