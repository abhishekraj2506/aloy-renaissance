package com.aloy.coreapp.model;

import com.aloy.coreapp.dto.nft.NftResponseDTO;
import com.aloy.coreapp.enums.BadgeType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
@Builder
public class Badge {

    @Id
    private String id;

    private String name;
    private String description;
    private String userId;
    private BadgeType badgeType;
    private Date createdAt;
    private NftResponseDTO nftData;
    private String imageUri;
}
