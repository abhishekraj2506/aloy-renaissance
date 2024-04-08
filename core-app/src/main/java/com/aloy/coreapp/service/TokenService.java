package com.aloy.coreapp.service;


import com.aloy.coreapp.dto.TokenDTO;
import com.aloy.coreapp.dto.UserAuthDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class TokenService {

    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    private static final long DEFAULT_TOKEN_EXPIRY_DAYS = 180;

    @Value("${jwt.payload.secret.key}")
    private String JWT_PAYLOAD_SECRET_KEY;

    private String createUserAccessToken(UserAuthDTO userAuthDTO) {
        Key deserializedKey = this.deserializeKey(JWT_PAYLOAD_SECRET_KEY);
        return Jwts.builder().setSubject(String.valueOf(userAuthDTO.getUserId()))
                .claim("userId", userAuthDTO.getUserId())
                .setExpiration(Date.from(Instant.ofEpochMilli(userAuthDTO.getExpiresAt())))
                .signWith(this.getSignatureAlgorithm(), deserializedKey).compact();
    }

    public UserAuthDTO parseUserFromToken(String token) {
        UserAuthDTO userAuth = null;
        Key deserializedKey = this.deserializeKey(JWT_PAYLOAD_SECRET_KEY);
        if (null != token) {
            try {
                Claims claims = (Claims) Jwts.parser().setSigningKey(deserializedKey).parseClaimsJws(token).getBody();
                userAuth = new UserAuthDTO();
                userAuth.setUserId(claims.getSubject());
                userAuth.setExpiresAt(claims.getExpiration().getTime());
                return userAuth;
            } catch (Exception ex) {
                log.error("Exception occurred while parsing access token.", ex);
            }
        }
        return userAuth;
    }

    public TokenDTO getTokenForUser(String userId) {
        UserAuthDTO userAuthDTO = getUserAuth(userId);
        String token = createUserAccessToken(userAuthDTO);
        return TokenDTO.builder().token(token).expiresAt(userAuthDTO.getExpiresAt())
                .build();
    }

    private UserAuthDTO getUserAuth(String userId) {
        UserAuthDTO userAuth = new UserAuthDTO();
        userAuth.setUserId(userId);
        long tokenCreatedMillis = System.currentTimeMillis();
        userAuth.setCreatedAt(tokenCreatedMillis);
        long tokenExpiryMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(DEFAULT_TOKEN_EXPIRY_DAYS);
        userAuth.setExpiresAt(tokenExpiryMillis);
        return userAuth;
    }

    private Key deserializeKey(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, this.getSignatureAlgorithm().getJcaName());
    }

    private SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

}

