package com.aloy.coreapp.config;


import com.aloy.coreapp.handler.CustomerWebSocketHandler;
import com.aloy.coreapp.interceptor.CustomerWSHandshakeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    @Bean
    public CustomerWebSocketHandler wsHandler() {
        return new CustomerWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsHandler(), "/ws/v1/user/globalWs")
                .setAllowedOrigins("*")
                .addInterceptors(new CustomerWSHandshakeInterceptor());
    }
}
