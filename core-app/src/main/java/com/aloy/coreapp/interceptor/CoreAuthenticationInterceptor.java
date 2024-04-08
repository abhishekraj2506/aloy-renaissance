package com.aloy.coreapp.interceptor;


import com.aloy.coreapp.context.UserContext;
import com.aloy.coreapp.dto.ValidateAccessTokenResponseDTO;
import com.aloy.coreapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class CoreAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {

        String accessToken = getAccessToken(request);
        String responseToClient = null;
        UserContext userContext = UserContext.current();

        if (StringUtils.isEmpty(accessToken) || StringUtils.isBlank(accessToken)) {
            responseToClient = "No access token sent in request.";
            return unauthorizedAccess(response, responseToClient);
        }
        String clientIpAddress = request.getRemoteAddr();

        try {
            userContext.setToken(accessToken);
            ValidateAccessTokenResponseDTO validationResponse = userService.validateToken(accessToken);

            userContext.setAccessRoleSet(Stream.of("USER")
                    .collect(Collectors.toCollection(HashSet::new)));
            userContext.setUserId(validationResponse.getUserId());
            userContext.setName(validationResponse.getName());
            MDC.put("request_id", UUID.randomUUID().toString().replace("-", ""));
            MDC.put("user_id", validationResponse.getUserId());
            return true;
        } catch (Exception e) {
            log.error("Could not authorise, exception occurred in pre handle - " + ExceptionUtils.getStackTrace(e));
            log.error("401 unauthorized for token: " + accessToken);
            responseToClient = "Invalid token";
            return unauthorizedAccess(response, responseToClient);
        }
    }

    private String getAccessToken(HttpServletRequest request) {
        String authToken = request.getHeader("Authorization");
        return authToken != null && authToken.length() > 0 ? authToken : null;
    }

    private boolean unauthorizedAccess(HttpServletResponse response, String responseToClient) {
        log.error(responseToClient);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeResponseData(response, responseToClient);
        return false;
    }

    private void writeResponseData(HttpServletResponse response, String responseData) {
        try {
            response.getWriter().write(responseData);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("Error in writing response data: " + ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) {
        MDC.clear();
    }

}