package com.aloy.coreapp.dto;

import com.aloy.coreapp.enums.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeDTO {
    private String userId;
    private String name;
    private String description;
    private BadgeType badgeType;
    private String imageUri;
}
