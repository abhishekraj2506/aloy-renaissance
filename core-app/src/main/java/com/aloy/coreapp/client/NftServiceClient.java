package com.aloy.coreapp.client;

import com.aloy.coreapp.dto.nft.BadgeNftRequestDTO;
import com.aloy.coreapp.dto.nft.ProfileNftRequestDTO;
import com.aloy.coreapp.dto.nft.NftResponseDTO;
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
public class NftServiceClient {

    @Autowired
    private OkHttpClient longPollClient;

    @Autowired
    private ObjectMapper om;

    @Value("${nft.service.base.url}")
    private String baseUrl;

    private static final MediaType mediaType = MediaType.parse("application/json");

    public NftResponseDTO createOrUpdateProfileNft(ProfileNftRequestDTO requestDTO) {
        log.info("Request to nft service api {}", requestDTO);
        RequestBody body = null;
        try {
            body = RequestBody.create(om.writeValueAsString(requestDTO), mediaType);
            String method = requestDTO.getBadgeData().getNftAddress() == null ? "POST" : "PUT";
            Request request = new Request.Builder()
                    .url(baseUrl.concat("/profileNft"))
                    .method(method, body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = longPollClient.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("Received response from nft service {}", responseBody);
            if (!response.isSuccessful()) {
                throw new CoreServiceException("Request to nft service was unsuccessful");
            }
            return om.readValue(responseBody, NftResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to send authenticate to nft service, error: " + ExceptionUtils.getStackTrace(e));
            throw new CoreServiceException("Failed with nft service");
        }
    }

    public NftResponseDTO createBadgeNft(BadgeNftRequestDTO requestDTO) {
        log.info("Request to badge nft service api {}", requestDTO);
        RequestBody body = null;
        try {
            body = RequestBody.create(om.writeValueAsString(requestDTO), mediaType);
            Request request = new Request.Builder()
                    .url(baseUrl.concat("/badge"))
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = longPollClient.newCall(request).execute();
            String responseBody = response.body().string();
            log.info("Received response from nft service {}", responseBody);
            if (!response.isSuccessful()) {
                throw new CoreServiceException("Request to nft service was unsuccessful");
            }
            return om.readValue(responseBody, NftResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to send badge request to nft service, error: " + ExceptionUtils.getStackTrace(e));
            throw new CoreServiceException("Failed with nft service");
        }
    }

}
