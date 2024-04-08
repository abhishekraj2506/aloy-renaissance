package com.aloy.coreapp.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CustomerWSHandshakeInterceptor extends HttpSessionHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        HttpHeaders headers = request.getHeaders();
        List<String> authorizationHeaders = headers.get(HttpHeaders.AUTHORIZATION);
        // Call the superclass method to handle the handshake
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        // Nothing to do here after the handshake
        MDC.clear();
    }

    private String getAccessToken(String authToken) {
        return authToken != null && authToken.length() > 0 ? authToken : null;
    }

    private boolean unauthorizedAccess(ServerHttpResponse response, String responseToClient) {
        log.error(responseToClient);
        response.setStatusCode(HttpStatusCode.valueOf(401));
        writeResponseData(response, responseToClient);
        return false;
    }

    private void writeResponseData(ServerHttpResponse response, String responseData) {
        try {
            response.getBody().write(responseData.getBytes());
            response.getBody().flush();
        } catch (IOException e) {
            log.error("Error in writing response data: " + ExceptionUtils.getStackTrace(e));
        }
    }
}
