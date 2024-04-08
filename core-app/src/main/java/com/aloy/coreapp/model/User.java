package com.aloy.coreapp.model;

import com.aloy.coreapp.dto.okto.WalletDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String name;
    private String email;
    private String phoneNumber;
    private String avatar;
    private List<Address> addresses;
    private String accessToken;
    private String wsToken;
    private int availablePoints;
    private int lifetimePoints;
    private Date createdAt;
    private Date updatedAt;
    private boolean active = true;
    private WalletDTO wallet;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String uuid;
        private String gps;
        private String address;
        private String city;
        private String state;
        private String country;
        private String areaCode;
    }
}
