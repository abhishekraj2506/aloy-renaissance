package com.aloy.coreapp.client;

import com.aloy.coreapp.dto.okto.OktoAuthenticateRequestDTO;
import com.aloy.coreapp.dto.okto.OktoResponseDTO;
import com.aloy.coreapp.dto.okto.OktoSetPinRequestDTO;
import com.aloy.coreapp.exception.CoreServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OktoClient {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${okto.base.url}")
    private String baseUrl;

    @Value("${okto.api.key}")
    private String apiKey;

    private static final MediaType mediaType = MediaType.parse("application/json");

    public OktoResponseDTO authenticate(OktoAuthenticateRequestDTO requestDTO) {
        log.info("Request to okto api {}", requestDTO);
        RequestBody body = null;
        try {
            body = RequestBody.create(om.writeValueAsString(requestDTO), mediaType);
            Request request = new Request.Builder()
                    .url(baseUrl.concat("/authenticate"))
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("Received response from okto {}", responseBody);
            if (!response.isSuccessful()) {
                log.error("Request to okto was unsuccessful");
                throw new CoreServiceException("Request to Okto to authenticate was unsuccessful");
            }
            return om.readValue(responseBody, OktoResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to send authenticate to Okto, error: " + ExceptionUtils.getStackTrace(e));
            throw new CoreServiceException("Failed to authenticate with okto");
        }
    }

    public OktoResponseDTO setPin(OktoSetPinRequestDTO requestDTO) {
        log.info("Request to okto set pin {}", requestDTO);
        RequestBody body = null;
        try {
            body = RequestBody.create(om.writeValueAsString(requestDTO), mediaType);
            Request request = new Request.Builder()
                    .url(baseUrl.concat("/set_pin"))
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("Received response from okto {}", responseBody);
            if (!response.isSuccessful()) {
                log.error("Request to okto was unsuccessful");
                throw new CoreServiceException("Request to Okto to set pin was unsuccessful");
            }
            return om.readValue(responseBody, OktoResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to send set pin to Okto, error: " + ExceptionUtils.getStackTrace(e));
            throw new CoreServiceException("Failed to set pin with okto");
        }
    }

    public OktoResponseDTO createWallet(String bearerToken) {
        log.info("Request to create wallet");
        RequestBody body = RequestBody.create(mediaType, "");
        try {
            Request request = new Request.Builder()
                    .url(baseUrl.concat("/wallet"))
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("Received response from okto {}", responseBody);
            if (!response.isSuccessful()) {
                log.error("Request to okto was unsuccessful");
                throw new CoreServiceException("Request to Okto to create wallet unsuccessful");
            }
            return om.readValue(responseBody, OktoResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to send create wallet to Okto, error: " + ExceptionUtils.getStackTrace(e));
            throw new CoreServiceException("Failed to create wallet with okto");
        }
    }


}
