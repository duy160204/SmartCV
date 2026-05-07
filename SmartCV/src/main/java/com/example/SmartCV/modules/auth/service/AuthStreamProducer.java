package com.example.SmartCV.modules.auth.service;

import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import com.example.SmartCV.config.RedisStreamConfig;
import com.example.SmartCV.modules.auth.service.CustomOAuth2UserService.UserRegisteredEvent;
import com.example.SmartCV.modules.auth.service.CustomOAuth2UserService.OAuth2LoginEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthStreamProducer {

    private final RedisTemplate<String, Object> redisTemplate;

    public void sendUserRegistered(String email, String provider) {
        sendMessage("user-registered", email, provider);
    }

    public void sendOAuth2Login(String email, String provider) {
        sendMessage("oauth2-login", email, provider);
    }

    public void sendSubscriptionActivated(String email, String plan) {
        sendMessage("subscription-activated", email, plan);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        sendUserRegistered(event.email(), event.provider());
        sendSubscriptionActivated(event.email(), "FREE");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOAuth2Login(OAuth2LoginEvent event) {
        sendOAuth2Login(event.email(), event.provider());
    }

    private void sendMessage(String type, String email, String data) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("type", type);
            payload.put("email", email);
            payload.put("data", data);

            redisTemplate.opsForStream().add(RedisStreamConfig.STREAM_AUTH_EVENT, payload);
            log.info("Sent async event to Redis Stream: topic={}, type={}", RedisStreamConfig.STREAM_AUTH_EVENT, type);
        } catch (Exception e) {
            log.error("Failed to send Redis Stream event: {}", e.getMessage());
        }
    }
}
