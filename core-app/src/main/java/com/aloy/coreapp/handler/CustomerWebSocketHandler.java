package com.aloy.coreapp.handler;

import com.aloy.coreapp.context.UserContext;
import com.aloy.coreapp.dto.ValidateAccessTokenResponseDTO;
import org.slf4j.MDC;
import com.aloy.coreapp.service.UserService;
import com.aloy.coreapp.utils.GenericUtils;
import com.beust.jcommander.internal.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class CustomerWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private UserService userService;

    @Value("${ws.secret.key}")
    private String WS_SECRET_KEY;

    // Store active WebSocket sessions in a map for easy access
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private static final String STATIC_WS_CONNECTION_ID = "STATIC-WS";

    @SneakyThrows
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String queryString = session.getUri() == null ? null : session.getUri().getQuery();
        if (queryString == null) {
            log.error("Closing session, no query string.");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        Map<String, String> queryParams = Maps.newHashMap();
        String[] keyValuePairs = queryString.split("&");
        if (keyValuePairs.length == 0) {
            log.error("Closing session, no query params.");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        for (String keyValuePair : keyValuePairs) {
            String[] keyVal = keyValuePair.split("=");
            queryParams.put(keyVal[0], keyVal[1]);
        }

        String ticketId = queryParams.getOrDefault("ticketId", null);
        if (ticketId == null) {
            log.error("Closing session as ticketId not provided in connection request");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String accessToken = GenericUtils.decrypt(ticketId, WS_SECRET_KEY);

        if (accessToken == null) {
            log.error("Closing session as ticket could not be decrypted.");
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        ValidateAccessTokenResponseDTO validationResponse = userService.validateToken(accessToken);
        UserContext userContext = UserContext.current();

        userContext.setUserId(validationResponse.getUserId());
        userContext.setName(validationResponse.getName());
        MDC.put("user_id", validationResponse.getUserId());

        //Add the new WebSocket session to the sessions map when a connection is established
        String connectionId = "WS-" + validationResponse.getUserId();
        sessions.put(connectionId, session);
        log.info("Created a socket connection for user {}, session id {}", connectionId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        log.info(payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Remove the WebSocket session from the sessions map when the connection is closed
        log.info("Removing socket connection with session id {}", session.getId());
        sessions.remove(session.getId());
    }

    // Method to send a message to a specific WebSocket connection
    public void sendMessageToConnection(String connectionId, String message) {
        log.info("Trying to send message to socket connection id {}: {}", connectionId, message);
        WebSocketSession session = sessions.get(connectionId);
        if (session == null) {
            log.error("Could not find socket session for connection id {}", connectionId);
            return;
        }
        if (!session.isOpen()) {
            log.error("Socket session for connection id {} is not open, cannot send message", connectionId);
            return;
        }
        try {
            session.sendMessage(new TextMessage(message));
            log.info("Trying to send message to socket connection {}", connectionId);
        } catch (IOException e) {
            log.error("Failed to send websocket message {}, error: {} ", message, ExceptionUtils.getStackTrace(e));
        }
    }
}
