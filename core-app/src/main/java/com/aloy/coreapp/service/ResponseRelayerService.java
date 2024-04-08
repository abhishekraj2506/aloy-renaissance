package com.aloy.coreapp.service;

import com.aloy.coreapp.handler.CustomerWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResponseRelayerService {

    @Autowired
    private CustomerWebSocketHandler customerWebSocketHandler;





}
