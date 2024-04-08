package com.aloy.sellerbppservice.controller;

import com.aloy.sellerbppservice.messaging.RabbitMessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/ondc-seller/test/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {

    @Autowired
    private RabbitMessageConsumer rabbitMessageConsumer;

    @PostMapping("")
    public void testOndcRequest(@RequestBody String request) {
        rabbitMessageConsumer.consumeInputMessage(request);
    }
}
