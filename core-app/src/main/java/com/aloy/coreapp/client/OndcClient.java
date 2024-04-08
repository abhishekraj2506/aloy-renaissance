package com.aloy.coreapp.client;

import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.exception.CoreServiceException;
import com.aloy.coreapp.utils.GenericUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class OndcClient {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${aloy.bap.id}")
    private String bapId;

    @Value("${aloy.bap.uri}")
    private String bapUri;

    @Value("${aloy.proxy.client.uri}")
    private String proxyClientUri;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static final MediaType mediaType = MediaType.parse("application/json");

    private OndcContextDTO createContext(String action, String bppId, String bppUri) {
        return createContext(action, bppId, bppUri, null);
    }

    private OndcContextDTO createContext(String action, String bppId, String bppUri, String txnId) {
        OndcContextDTO ondcContextDTO = OndcContextDTO.builder().action(action).bap_id(bapId).bap_uri(bapUri)
                .domain("retail:1.1.0").version("1.1.0")
                .location(OndcContextDTO.ContextLocation.builder()
                        .city(OndcContextDTO.City.builder().code("std:080").build())
                        .country(OndcContextDTO.Country.builder().code("IND").build()).build())
                .message_id(GenericUtils.generateUuid().toString())
                .transaction_id(txnId == null ? GenericUtils.generateUuid().toString() : txnId)
                .timestamp(LocalDateTime.now().format(formatter)).build();
        if (action.equals("search")) {
            return ondcContextDTO;
        }
        ondcContextDTO.setBpp_id(bppId);
        ondcContextDTO.setBpp_uri(bppUri);
        return ondcContextDTO;
    }

    public void sendSearchRequest(OndcSearchRequestDTO searchRequestDTO, String bppId, String bppUri) {
        searchRequestDTO.setContext(createContext("search", bppId, bppUri));
        try {
            doSendRequest(om.writeValueAsString(searchRequestDTO), "search");
        } catch (JsonProcessingException e) {
            throw new CoreServiceException("Failed to initiate order request.");
        }
    }

    public String sendInitRequest(OndcInitRequestDTO ondcInitRequestDTO, String bppId, String bppUri) {
        ondcInitRequestDTO.setContext(createContext("init", bppId, bppUri));
        try {
            doSendRequest(om.writeValueAsString(ondcInitRequestDTO), "init");
        } catch (JsonProcessingException e) {
            throw new CoreServiceException("Failed to initiate order request.");
        }
        return ondcInitRequestDTO.getContext().getTransaction_id();
    }

    public String sendConfirmRequest(OndcConfirmRequestDTO confirmRequestDTO, String bppId, String bppUri,
                                   String ondcTransactionId) {
        confirmRequestDTO.setContext(createContext("confirm", bppId, bppUri, ondcTransactionId));
        try {
            doSendRequest(om.writeValueAsString(confirmRequestDTO), "confirm");
        } catch (JsonProcessingException e) {
            throw new CoreServiceException("Failed to initiate order request.");
        }
        return ondcTransactionId;
    }

    private void doSendRequest(String requestBody, String action) {
        log.info("Request to client proxy {}", requestBody);
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(proxyClientUri.concat("/" + action))
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Request to proxy client was unsuccessful");
                throw new CoreServiceException("Request to proxy client was unsuccessful");
            }
            String responseBody = response.body().string();
            log.info("Received response from client proxy {}", responseBody);
            OndcAckResponseDTO ackResponseDTO = om.readValue(responseBody, OndcAckResponseDTO.class);
            if (!ackResponseDTO.getMessage().getAck().getStatus().equals("ACK")) {
                throw new CoreServiceException("Did not receive an ack from client proxy");
            }
        } catch (CoreServiceException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Failed to send request to proxy, error: " + ExceptionUtils.getStackTrace(e));
            throw new CoreServiceException("Failed to communicate with client proxy");
        }
    }
}
