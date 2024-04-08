package com.aloy.coreapp.service;

import com.aloy.coreapp.dto.okto.WalletDTO;

public interface Web3WalletService {

    WalletDTO createWallet(String userId, String googleIdToken);
}
