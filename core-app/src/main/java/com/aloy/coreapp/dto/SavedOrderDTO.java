package com.aloy.coreapp.dto;

import com.aloy.coreapp.model.Orders;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SavedOrderDTO {
    private Orders order;
    private boolean newOrder;
}
