package com.aloy.coreapp.controller;

import com.aloy.coreapp.context.UserContext;
import com.aloy.coreapp.dto.CouponDTO;
import com.aloy.coreapp.dto.Response;
import com.aloy.coreapp.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/ondc-buyer/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping("/coupons")
    public Response<List<CouponDTO>> getAvailableCoupons() {
        return new Response<>(rewardService.getAvailableCoupons());
    }

    @PostMapping("/coupons/{couponId}/purchase")
    public Response<Boolean> purchaseCoupon(@PathVariable String couponId) {
        return new Response<>(rewardService.purchaseCoupon(UserContext.current().getUserId(), couponId));
    }

}
