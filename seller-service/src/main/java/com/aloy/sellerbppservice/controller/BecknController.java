package com.aloy.sellerbppservice.controller;


import com.aloy.sellerbppservice.dto.Response;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/ondc-seller/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
public class BecknController {

    @GetMapping("/search")
    @ResponseBody
    public Response<String> search(@RequestParam String phoneNumber) {
        return new Response<>("OTP sent successfully");
    }
}
