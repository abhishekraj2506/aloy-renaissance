package com.aloy.sellerbppservice.messaging;

import com.aloy.sellerbppservice.dto.*;
import com.aloy.sellerbppservice.service.RequestHandlerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMessageConsumer {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RequestHandlerService requestHandlerService;

    @Value("${aloy.bap.id}")
    private String testBapId;

    @RabbitListener(queues = {"${rabbitmq.input.queue.name}"})
    public void consumeInputMessage(String message) {
        log.info(String.format("Received message -> %s", message));
        try {
            OndcBaseRequestDTO ondcBaseRequestDTO = om.readValue(message, OndcBaseRequestDTO.class);
            if (!ondcBaseRequestDTO.getContext().getBap_id().equals(testBapId)) {
                log.info("Ignoring search response from external bap");
                return;
            }
            String action = ondcBaseRequestDTO.getContext().getAction();
            switch (action) {
                case "search" -> {
                    OndcSearchRequestDTO ondcSearchRequestDTO = om.readValue(message, OndcSearchRequestDTO.class);
                    requestHandlerService.handleSearch(ondcSearchRequestDTO);
                }
                case "init" -> {
                    OndcInitRequestDTO ondcInitRequestDTO = om.readValue(message, OndcInitRequestDTO.class);
                    requestHandlerService.handleInit(ondcInitRequestDTO);
                }
                case "confirm" -> {
                    OndcConfirmRequestDTO ondcConfirmRequestDTO = om.readValue(message, OndcConfirmRequestDTO.class);
                    requestHandlerService.handleConfirm(ondcConfirmRequestDTO);
                }
                default -> {
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to read message ", e);
        } catch (Exception e) {
            log.error("Failed to process rabbitmq message ", e);
        }
    }

}
