package com.aloy.coreapp.service.impl;

import com.aloy.coreapp.client.OktoClient;
import com.aloy.coreapp.dto.okto.OktoAuthenticateRequestDTO;
import com.aloy.coreapp.dto.okto.OktoResponseDTO;
import com.aloy.coreapp.dto.okto.OktoSetPinRequestDTO;
import com.aloy.coreapp.dto.okto.WalletDTO;
import com.aloy.coreapp.exception.CoreServiceException;
import com.aloy.coreapp.service.Web3WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class Web3WalletServiceImpl implements Web3WalletService {

    @Autowired
    private OktoClient oktoClient;

    @Override
    public WalletDTO createWallet(String userId, String googleIdToken) {
        try {
            OktoResponseDTO oktoResponseDTO = oktoClient.authenticate(OktoAuthenticateRequestDTO.builder().idToken(googleIdToken).build());
            if (oktoResponseDTO.getStatus().equals("error")) {
                log.error("Failed to authenticate with okto, error: {}", oktoResponseDTO.getError());
                throw new CoreServiceException("Failed to authenticate with Okto");
            }

            if (oktoResponseDTO.getData().getAuthToken() != null) {
                log.info("User already authenticated with okto");
                return createOktoWallet(userId, oktoResponseDTO.getData().getAuthToken(),
                        oktoResponseDTO.getData().getDeviceToken(),
                        oktoResponseDTO.getData().getRefreshAuthToken());
            }
            String token = oktoResponseDTO.getData().getToken();
            log.info("Received token from okto {}", token);
            OktoResponseDTO setPinResponse = oktoClient.setPin(OktoSetPinRequestDTO.builder()
                    .idToken(googleIdToken).token(token).relogin_pin("7625").purpose("set_pin").build());
            if (setPinResponse.getStatus().equals("error")) {
                log.error("Failed to set pin with okto, error: {}", oktoResponseDTO.getError());
                throw new CoreServiceException("Failed to set pin with Okto");
            }
            String authToken = setPinResponse.getData().getAuthToken();
            return createOktoWallet(userId, authToken, setPinResponse.getData().getDeviceToken(),
                    setPinResponse.getData().getRefreshAuthToken());
        } catch (CoreServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create wallet, error: {}", e.getMessage());
            throw new CoreServiceException("Failed to create web3 wallet");
        }
    }

    private WalletDTO createOktoWallet(String userId, String authToken, String deviceToken, String refreshAuthToken) {
        OktoResponseDTO wallet = oktoClient.createWallet(authToken);
        if (wallet.getStatus().equals("error")) {
            log.error("Failed to create wallet with okto, error: {}", wallet.getError());
            throw new CoreServiceException("Failed to create wallet with Okto");
        }
        log.info("Wallet created successfully for user {}", userId);
        log.info("Wallets: {}", wallet.getData().getWallets());
        Optional<OktoResponseDTO.Data.Wallet> solanaDevnet = wallet.getData().getWallets().stream()
                .filter(w -> w.getNetworkName().equals("SOLANA_DEVNET")).findFirst();
        if (solanaDevnet.isEmpty()) {
            log.error("Failed to find solana devnet wallet");
            throw new CoreServiceException("Failed to find solana devnet wallet");
        }
        return WalletDTO.builder().deviceToken(deviceToken)
                .refreshAuthToken(refreshAuthToken)
                .accessToken(authToken).client("okto")
                .walletAddress(solanaDevnet.get().getAddress()).build();
    }
}
