package com.aloy.coreapp.controller;

import com.aloy.coreapp.context.UserContext;
import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.exception.CoreServiceException;
import com.aloy.coreapp.model.Badge;
import com.aloy.coreapp.model.RetroReward;
import com.aloy.coreapp.model.User;
import com.aloy.coreapp.service.RewardService;
import com.aloy.coreapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/ondc-buyer/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RewardService rewardService;

    @GetMapping("/profile")
    public Response<UserDTO> getUserProfile() {
        return new Response<>(userService.getUserProfile(UserContext.current().getUserId()));
    }

    @PostMapping("/login")
    public Response<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO loginRequestDTO) {
        return new Response<>(userService.login(loginRequestDTO));
    }

    @GetMapping("/coupons/all")
    public Response<List<UserCouponDTO>> getUserCoupons() {
        return new Response<>(rewardService.getUserCoupons(UserContext.current().getUserId()));
    }

    @GetMapping("/coupons/applicable")
    public Response<List<UserCouponDTO>> getApplicableCoupons(@RequestParam String sellerId,
                                                              @RequestParam String orderAmount) {
        return new Response<>(rewardService.getApplicableCoupons(UserContext.current().getUserId(), sellerId, orderAmount));
    }

    @PostMapping("/confirmPhoneNumber")
    public Response<UserDTO> confirmPhoneNumber(@RequestBody ConfirmPhoneRequestDTO confirmPhoneRequestDTO) {
        return new Response<>(userService.addPhoneNumber(UserContext.current().getUserId(),
                confirmPhoneRequestDTO.getPhoneNumber(), confirmPhoneRequestDTO.getOtp()));
    }

    @PostMapping("/retroReward")
    public Response<RetroReward> addRetroReward(@RequestBody RetroRewardDTO retroRewardDTO) {
//        if (!"5SDD9pSSqZlqF7mAhiOgRbgv9Sm".equals(retroRewardDTO.getKey())) {
//            throw new CoreServiceException("Cannot add reward");
//        }
        retroRewardDTO.setUserId(UserContext.current().getUserId());
        return new Response<>(rewardService.addRetroReward(retroRewardDTO));
    }

    @PostMapping("/address")
    public Response<List<User.Address>> addAddress(@RequestBody User.Address address) {
        return new Response<>(userService.addAddress(UserContext.current().getUserId(), address));
    }

    @GetMapping("/address")
    public Response<List<User.Address>> getAddresses() {
        return new Response<>(userService.getById(UserContext.current().getUserId()).getAddresses());
    }

    @GetMapping("/badges")
    public Response<List<Badge>> getUserBadges() {
        return new Response<>(rewardService.getUserBadges(UserContext.current().getUserId()));
    }

//    @GetMapping("/badge/{id}")
//    public Response<UserBadgeDTO> getUserBadge(@PathVariable String id) {
//        return new Response<>(rewardService.getUserBadge(UserContext.current().getUserId(), id));
//    }

}
