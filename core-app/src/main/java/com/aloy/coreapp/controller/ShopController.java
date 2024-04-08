package com.aloy.coreapp.controller;

import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/ondc-buyer/api/v1/shop", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("/syncCatalog")
    @ResponseBody
    public Response<Boolean> syncCatalog() {
        return new Response<>(shopService.syncCatalog());
    }

    @GetMapping("/sellers")
    @ResponseBody
    public Response<List<SellerDTO>> getSellers() {
        return new Response<>(shopService.getSellers());
    }

    @GetMapping("/seller/{id}/items")
    @ResponseBody
    public Response<List<SellerItemDTO>> getSellerItems(@PathVariable String id) {
        return new Response<>(shopService.getSellerItems(id));
    }

    @PostMapping("/order/initiate")
    @ResponseBody
    public Response<InitiateOrderResponseDTO> initiateOrder(@RequestBody InitiateOrderRequestDTO initiateOrderRequestDTO) {
        log.info("Received initiate order request {}", initiateOrderRequestDTO);
        return new Response<>(shopService.initiateOrder(initiateOrderRequestDTO));
    }

    @PostMapping("/order/confirm")
    @ResponseBody
    public Response<ConfirmOrderResponseDTO> confirmOrder(@RequestBody ConfirmOrderRequestDTO confirmOrderRequestDTO) {
        log.info("Received confirm order request {}", confirmOrderRequestDTO);
        return new Response<>(shopService.confirmOrder(confirmOrderRequestDTO));
    }

}
