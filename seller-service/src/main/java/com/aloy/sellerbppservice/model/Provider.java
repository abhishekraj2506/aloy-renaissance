package com.aloy.sellerbppservice.model;

import com.aloy.sellerbppservice.dto.OndcCommonDTO;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "provider")
public class Provider {
    @Id
    private String id;

    private String name;
    private OndcCommonDTO.Descriptor descriptor;
    private List<Location> locations;
    private List<Category> categories;
    private List<Fulfillment> fulfillments;
    private boolean isActive = true;

    @Data
    public static final class Location {
        @SerializedName("id")
        private String id;
        private String gps;
    }

    @Data
    public static final class Category {
        @SerializedName("id")
        private String id;
        private CategoryDescriptor descriptor;
    }

    @Data
    public static final class CategoryDescriptor {
        private String code;
        private String name;
    }

    @Data
    public static final class Fulfillment {
        @SerializedName("id")
        private String id;
        private String type;
    }

}


