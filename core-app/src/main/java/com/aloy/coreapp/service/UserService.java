package com.aloy.coreapp.service;

import com.aloy.coreapp.dto.UserDTO;
import com.aloy.coreapp.dto.UserLoginRequestDTO;
import com.aloy.coreapp.dto.UserLoginResponseDTO;
import com.aloy.coreapp.dto.ValidateAccessTokenResponseDTO;
import com.aloy.coreapp.dto.okto.WalletDTO;
import com.aloy.coreapp.model.User;

import java.util.List;

public interface UserService {

    User createUser(UserDTO userDTO);

    User getById(String userId);

    User getByPhoneNumber(String phoneNumber);

    ValidateAccessTokenResponseDTO validateToken(String accessToken);

    void addPoints(String userId, int points);

    UserDTO getUserProfile(String userId);

    UserLoginResponseDTO login(UserLoginRequestDTO loginRequestDTO);

    UserDTO addPhoneNumber(String userId, String phoneNumber, String otp);

    User addWallet(String userId, WalletDTO walletDTO);

    List<User.Address> addAddress(String userId, User.Address address);
}
