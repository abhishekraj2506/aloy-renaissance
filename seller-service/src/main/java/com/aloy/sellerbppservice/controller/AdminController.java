package com.aloy.sellerbppservice.controller;

import com.aloy.sellerbppservice.dto.Response;
import com.aloy.sellerbppservice.model.Coupon;
import com.aloy.sellerbppservice.model.Item;
import com.aloy.sellerbppservice.model.Provider;
import com.aloy.sellerbppservice.service.CouponService;
import com.aloy.sellerbppservice.service.ItemService;
import com.aloy.sellerbppservice.service.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/ondc-seller/api/v1/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@Controller
@Slf4j
public class AdminController {

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CouponService couponService;

    @PostMapping("/provider")
    @ResponseBody
    public Response<String> addProvider(@RequestBody Provider provider) {
        Provider savedProvider = providerService.add(provider);
        return new Response<>("Added " + savedProvider.getId());
    }

    @PostMapping("/item")
    @ResponseBody
    public Response<String> addItem(@RequestBody Item item) {
        Item savedItem = itemService.addItem(item);
        return new Response<>("Added " + savedItem.getId());
    }


    @GetMapping("/providers")
    @ResponseBody
    public Response<List<Provider>> addProvider() {
        return new Response<>(providerService.findActiveProviders());
    }

    @PostMapping("/coupon")
    @ResponseBody
    public Response<Coupon> addCoupon(@RequestBody Coupon coupon) {
        Coupon addedCoupon = couponService.create(coupon);
        return new Response<>(addedCoupon);
    }

    @PostMapping("/coupon-usage")
    @ResponseBody
    public Response<List<String>> addCouponUsage(@RequestParam("couponId") String couponId,
                                           @RequestParam("numberOfCoupons") int numberOfCoupons) {
        return new Response<>(couponService.addCouponsToUsage(couponId, numberOfCoupons));
    }

}
