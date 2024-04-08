package com.aloy.coreapp.client;

import com.aloy.coreapp.dto.CreateNFTRequestDTO;
import com.aloy.coreapp.dto.CreateNFTResponseDTO;
import com.aloy.coreapp.exception.CoreServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UnderdogClient {

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${underdog.base.url}")
    private String baseUrl;

    @Value("${underdog.api.key}")
    private String apiKey;

    @Value("${underdog.project.id}")
    private String projectId;

    private static final MediaType mediaType = MediaType.parse("application/json");

    public CreateNFTResponseDTO mintNft(CreateNFTRequestDTO nftRequestDTO) {
        log.info("Request to underdog api {}", nftRequestDTO);
        RequestBody body = null;
        try {
            body = RequestBody.create(om.writeValueAsString(nftRequestDTO), mediaType);
            Request request = new Request.Builder()
                    .url(baseUrl.concat("/projects/" + projectId + "/nfts"))
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer ".concat(apiKey))
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Request to underdog was unsuccessful");
                throw new CoreServiceException("Request to proxy client was unsuccessful");
            }
            String responseBody = response.body().string();
            log.info("Received response from underdog {}", responseBody);
            return om.readValue(responseBody, CreateNFTResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to create request to underdog, error: " + ExceptionUtils.getStackTrace(e));
//            throw new CoreServiceException("Failed to create request to underdog");
            return null;
        }
    }
}
