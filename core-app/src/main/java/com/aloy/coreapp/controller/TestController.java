package com.aloy.coreapp.controller;

import com.aloy.coreapp.client.NftServiceClient;
import com.aloy.coreapp.client.UnderdogClient;
import com.aloy.coreapp.dto.CreateNFTRequestDTO;
import com.aloy.coreapp.dto.Response;
import com.aloy.coreapp.dto.UserDTO;
import com.aloy.coreapp.dto.UserLoginRequestDTO;
import com.aloy.coreapp.dto.nft.BadgeNftRequestDTO;
import com.aloy.coreapp.dto.nft.NftResponseDTO;
import com.aloy.coreapp.dto.nft.ProfileNftRequestDTO;
import com.aloy.coreapp.messaging.RabbitMessageConsumer;
import com.aloy.coreapp.model.Coupon;
import com.aloy.coreapp.model.User;
import com.aloy.coreapp.service.RewardService;
import com.aloy.coreapp.service.ShopService;
import com.aloy.coreapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/ondc-buyer/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {

    @Autowired
    private UserService userService;

    @Autowired
    private RabbitMessageConsumer messageConsumer;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UnderdogClient underdogClient;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private NftServiceClient nftServiceClient;

    @PostMapping("/user")
    @ResponseBody
    public Response<User> addUser(@RequestBody UserDTO userDTO) {
        return new Response<>(userService.createUser(userDTO));
    }

    @PostMapping("/ondc-response")
    public void handleNetworkResponse(@RequestBody String networkResponse) {
        messageConsumer.consumeInputMessage(networkResponse);
    }

    @GetMapping("/syncCatalog")
    public void syncCatalog() {
        shopService.syncCatalog();
    }

    @PostMapping("/nft")
    public void createNft(@RequestBody CreateNFTRequestDTO createNFTRequestDTO) {
        underdogClient.mintNft(createNFTRequestDTO);
    }

    @PostMapping("/login")
    public void loginTest(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {
        userService.login(userLoginRequestDTO);
    }

    @PostMapping("/coupon")
    public Response<Coupon> addCoupon(@RequestBody Coupon coupon) {
        return new Response<>(rewardService.addCoupon(coupon));
    }

    @PostMapping("/profileNft/{address}")
    public Response<NftResponseDTO> createNft(@PathVariable String address) {
        return new Response<>(nftServiceClient.createOrUpdateProfileNft(
                ProfileNftRequestDTO.builder()
                        .badgeData(
                                ProfileNftRequestDTO.BadgeData.builder()
                                        .attributes(List.of(
                                                ProfileNftRequestDTO.Attribute.builder().traitType("currentPoints").value("0").build(),
                                                ProfileNftRequestDTO.Attribute.builder().traitType("lifetimePoints").value("0").build()))
                                        .build()
                        )
                        .receiverAddress(address)
                        .build()));
    }

    @PostMapping("/badge/{address}")
    public Response<NftResponseDTO> createBadge(@PathVariable String address) {
        return new Response<>(nftServiceClient.createBadgeNft(
                BadgeNftRequestDTO.builder()
                        .badgeData(
                                BadgeNftRequestDTO.BadgeData.builder()
                                        .name("Aloy Explorer")
                                        .description("First Order NFT")
                                        .imageFile("explorer.png")
                                        .attributes(List.of(
                                                BadgeNftRequestDTO.Attribute.builder().traitType("Explorer")
                                                        .value("Level 1").build()))
                                        .build()
                        )
                        .receiverAddress(address)
                        .build()));
    }
}
