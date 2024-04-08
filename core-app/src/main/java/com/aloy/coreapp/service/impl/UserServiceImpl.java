package com.aloy.coreapp.service.impl;

import com.aloy.coreapp.client.OktoClient;
import com.aloy.coreapp.dto.*;
import com.aloy.coreapp.dto.nft.NftResponseDTO;
import com.aloy.coreapp.dto.nft.ProfileNftRequestDTO;
import com.aloy.coreapp.dto.okto.WalletDTO;
import com.aloy.coreapp.dto.rabbit.CreateWalletRabbitMessageDTO;
import com.aloy.coreapp.dto.rabbit.UpdateProfileNftMessageDTO;
import com.aloy.coreapp.enums.OrderStatus;
import com.aloy.coreapp.enums.TaskType;
import com.aloy.coreapp.exception.CoreServiceException;
import com.aloy.coreapp.messaging.RabbitMessageProducer;
import com.aloy.coreapp.model.Orders;
import com.aloy.coreapp.model.User;
import com.aloy.coreapp.repos.OrderRepository;
import com.aloy.coreapp.repos.UserRepository;
import com.aloy.coreapp.service.TokenService;
import com.aloy.coreapp.service.UserService;
import com.aloy.coreapp.utils.GenericUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Clock;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RabbitMessageProducer rabbitMessageProducer;

    @Autowired
    private ObjectMapper om;


    @Value("${ws.secret.key}")
    private String WS_SECRET_KEY;

    //    private static final String CLIENT_ID = "587333044005-6m8ph0aioutf4umr71udftig3ild3g7s.apps.googleusercontent.com";
    private static final String CLIENT_ID = "587333044005-f4pgc2ffhj6sudoe8aj0tea78ee16d5s.apps.googleusercontent.com";
    private static final ApacheHttpTransport.Builder builder = new ApacheHttpTransport.Builder();
    private static final JacksonFactory jacksonFactory = new JacksonFactory();

    private static GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(builder.build(), jacksonFactory)
            // Specify the CLIENT_ID of the app that accesses the backend:
            .setAudience(Collections.singletonList(CLIENT_ID))
            .setClock(Clock.SYSTEM)
            .build();

    private static final String STATIC_OTP = "123456";

    @Override
    public User createUser(UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findByPhoneNumberAndActiveIsTrue(userDTO.getPhoneNumber());
        if (existingUser.isPresent()) return existingUser.get();

        User user = new User();
        user.setActive(true);
        user.setCreatedAt(Date.from(Instant.now()));
        user.setName(userDTO.getName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddresses(List.of(User.Address
                .builder().gps(userDTO.getGps()).address(userDTO.getAddress())
                .state(userDTO.getState()).city(userDTO.getCity())
                .country("IND").areaCode(userDTO.getAreaCode())
                .uuid(GenericUtils.generateUuid().toString()).build()));
        user.setEmail(userDTO.getEmail());
        user = userRepository.save(user);
        //Add tokens
        TokenDTO tokenDTO = tokenService.getTokenForUser(user.getId());
        user.setAccessToken(tokenDTO.getToken());
        user.setWsToken(GenericUtils.encrypt(tokenDTO.getToken(), WS_SECRET_KEY));
        user.setAvailablePoints(0);
        user.setLifetimePoints(0);
        user.setUpdatedAt(Date.from(Instant.now()));
        return userRepository.save(user);
    }

    @Override
    public User getById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CoreServiceException("User not found"));
    }

    @Override
    public User getByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumberAndActiveIsTrue(phoneNumber)
                .orElseThrow(() -> new CoreServiceException("No user found with number " + phoneNumber));

    }

    public ValidateAccessTokenResponseDTO validateToken(String accessToken) {
        UserAuthDTO userAuth = tokenService.parseUserFromToken(accessToken);
        if (userAuth == null) {
            throw new CoreServiceException("Invalid access token");
        }
        if (userAuth.getExpiresAt() < System.currentTimeMillis()) {
            throw new CoreServiceException("Token has expired.");
        }
        Optional<User> userOptional = userRepository.findById(userAuth.getUserId());
        if (userOptional.isEmpty()) {
            throw new CoreServiceException("User does not exist.");
        }
        User user = userOptional.get();
        if (!user.getAccessToken().equals(accessToken)) {
            throw new CoreServiceException("Use latest token");
        }
        if (!user.isActive()) {
            throw new CoreServiceException("User deactivated");
        }
        return ValidateAccessTokenResponseDTO.builder().userId(userAuth.getUserId()).build();
    }

    @Override
    public void addPoints(String userId, int points) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new CoreServiceException("User not found");
        }
        User u = user.get();
        u.setAvailablePoints(u.getAvailablePoints() + points);
        if (points > 0)
            u.setLifetimePoints(u.getLifetimePoints() + points);
        u.setUpdatedAt(Date.from(Instant.now()));
        userRepository.save(u);
    }

    @Override
    public UserDTO getUserProfile(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new CoreServiceException("User not found");
        }
        User user = userOptional.get();
        return toDto(user);
    }

    private UserDTO toDto(User user) {
        return UserDTO.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .availablePoints(user.getAvailablePoints())
                .lifetimePoints(user.getLifetimePoints())
                .build();
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO loginRequestDTO) {
        if (StringUtils.isEmpty(loginRequestDTO.getAuthCode())) {
            throw new CoreServiceException("Invalid authentication code provided.");
        }
        try {
            GoogleIdToken idToken = verifier.verify(loginRequestDTO.getAuthCode());

            if (idToken == null) {
                throw new CoreServiceException("Unable to login with google");
            }
            log.info("Google login successful, idToken: {}", idToken);
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Get profile information from payload
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String imageUrl = (String) payload.get("picture");

            Optional<User> optionalUser = userRepository.findByEmailAndActiveIsTrue(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                TokenDTO tokenDTO = tokenService.getTokenForUser(user.getId());
                user.setAccessToken(tokenDTO.getToken());
                user.setWsToken(GenericUtils.encrypt(tokenDTO.getToken(), WS_SECRET_KEY));
                user = userRepository.save(user);
                return UserLoginResponseDTO.builder()
                        .isNewUser(false).wsToken(user.getWsToken()).accessToken(user.getAccessToken())
                        .name(user.getName()).email(user.getEmail()).build();
            } else {
                User user = new User();
                user.setActive(true);
                user.setCreatedAt(Date.from(Instant.now()));
                user.setAvatar(imageUrl);
                user.setName(name);
                user.setEmail(email);
                user.setAvailablePoints(0);
                user.setLifetimePoints(0);
                user = userRepository.save(user);
                //Add tokens
                TokenDTO tokenDTO = tokenService.getTokenForUser(user.getId());
                user.setAccessToken(tokenDTO.getToken());
                user.setWsToken(GenericUtils.encrypt(tokenDTO.getToken(), WS_SECRET_KEY));
                user.setUpdatedAt(Date.from(Instant.now()));
                user = userRepository.save(user);
                RabbitTaskMessageDTO<CreateWalletRabbitMessageDTO> messageDTO = new RabbitTaskMessageDTO<>();
                messageDTO.setType(TaskType.CREATE_WALLET);
                messageDTO.setData(CreateWalletRabbitMessageDTO.builder()
                        .userId(user.getId()).googleIdToken(loginRequestDTO.getAuthCode()).mintNft(true).build());
                rabbitMessageProducer.sendMessage(om.writeValueAsString(messageDTO));
                return UserLoginResponseDTO.builder()
                        .isNewUser(true).wsToken(user.getWsToken()).accessToken(user.getAccessToken())
                        .name(user.getName()).email(user.getEmail()).build();
            }
        } catch (Exception e) {
            log.error("Unable to authenticate using Google: {}", ExceptionUtils.getFullStackTrace(e));
            throw new CoreServiceException("Unable to authenticate using Google.");
        }
    }

    @Override
    public UserDTO addPhoneNumber(String userId, String phoneNumber, String otp) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new CoreServiceException("User not found");
        }
        //validate otp
        if (!otp.equals(STATIC_OTP)) {
            throw new CoreServiceException("Invalid OTP");
        }
        User u = user.get();
        if (u.getPhoneNumber() != null) {
            throw new CoreServiceException("Phone number already added");
        }
        userRepository.findByPhoneNumber(phoneNumber).ifPresent(ux -> {
            throw new CoreServiceException("Phone number already in use");
        });
        u.setPhoneNumber(phoneNumber);
        u.setUpdatedAt(Date.from(Instant.now()));
        return toDto(userRepository.save(u));
    }

    @Override
    public User addWallet(String userId, WalletDTO walletDTO) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new CoreServiceException("User not found");
        }
        User u = user.get();
        u.setWallet(walletDTO);
        return userRepository.save(u);
    }

    @Override
    public List<User.Address> addAddress(String userId, User.Address address) {
        address.setCountry("IN");
        address.setUuid(GenericUtils.generateUuid().toString());
        if (address.getGps() == null) {
            address.setGps("13.2008459,77.708736");
        }
        if (address.getCity() == null) {
            address.setCity("Bengaluru");
        }
        if (address.getState() == null) {
            address.setState("Karnataka");
        }
        if (address.getAreaCode() == null) {
            address.setAreaCode("560060");
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new CoreServiceException("User not found");
        }
        User u = user.get();
        List<User.Address> addresses = u.getAddresses();
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        addresses.add(address);
        u.setAddresses(addresses);
        u.setUpdatedAt(Date.from(Instant.now()));
        userRepository.save(u);
        return addresses;
    }

}
