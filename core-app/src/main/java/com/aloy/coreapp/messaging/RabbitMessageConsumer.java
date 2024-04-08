package com.aloy.coreapp.messaging;

import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.dto.rabbit.CreateWalletRabbitMessageDTO;
import com.aloy.coreapp.dto.rabbit.UpdateProfileNftMessageDTO;
import com.aloy.coreapp.enums.TaskType;
import com.aloy.coreapp.service.RequestHandlerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

@Component
@Slf4j
public class RabbitMessageConsumer {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RequestHandlerService requestHandlerService;

    @Value("${aloy.bpp.id}")
    private String testBppId;

    @RabbitListener(queues = {"${rabbitmq.input.queue.name}"})
    public void consumeInputMessage(String message) {
        log.info(String.format("Received message -> %s", message));
        try {
            OndcBaseResponseDTO ondcBaseResponseDTO = om.readValue(message, OndcBaseResponseDTO.class);
            //Controlled test env, only entertain response from
            if (!ondcBaseResponseDTO.getContext().getBpp_id().equals(testBppId)) {
                log.info("Ignoring search response from external bpp");
                return;
            }
            String action = ondcBaseResponseDTO.getContext().getAction();
            switch (action) {
                case "on_search" -> {
                    OndcSearchResponseDTO searchResponseDTO = om.readValue(message, OndcSearchResponseDTO.class);
                    requestHandlerService.handleSearchResponse(searchResponseDTO);
                }
                case "on_init" -> {
                    OndcInitResponseDTO initResponseDTO = om.readValue(message, OndcInitResponseDTO.class);
                    requestHandlerService.handleInitResponse(initResponseDTO);
                }
                case "on_confirm" -> {
                    OndcConfirmResponseDTO confirmResponseDTO = om.readValue(message, OndcConfirmResponseDTO.class);
                    requestHandlerService.handleConfirmResponse(confirmResponseDTO);
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

    @RabbitListener(queues = {"${rabbitmq.task.queue.name}"})
    public void consumeTaskMessage(String message) {
        log.info(String.format("Received task message -> %s", message));
        try {
            Map<String, Object> map = om.readValue(message, Map.class);
            for (String key : map.keySet()) {
                if (!key.equals("type")) {
                    continue;
                }
                TaskType taskType = TaskType.valueOf((String) map.get(key));
                if (taskType == TaskType.CREATE_WALLET) {
                    RabbitTaskMessageDTO<CreateWalletRabbitMessageDTO> messageDTO = om.readValue(message,
                            new TypeReference<RabbitTaskMessageDTO<CreateWalletRabbitMessageDTO>>(){});
                    requestHandlerService.handleCreateWalletTask(messageDTO);
                } else if (taskType == TaskType.UPDATE_PROFILE_NFT) {
                    RabbitTaskMessageDTO<UpdateProfileNftMessageDTO> messageDTO = om.readValue(message,
                            new TypeReference<RabbitTaskMessageDTO<UpdateProfileNftMessageDTO>>() {
                            });
                    requestHandlerService.handleUpdateProfileNftTask(messageDTO);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to read message ", e);
        } catch (Exception e) {
            log.error("Failed to process rabbitmq message ", e);
        }
    }

}
