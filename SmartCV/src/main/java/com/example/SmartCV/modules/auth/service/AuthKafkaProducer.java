package com.example.SmartCV.modules.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import com.example.SmartCV.modules.auth.service.CustomOAuth2UserService.UserRegisteredEvent;
import com.example.SmartCV.modules.auth.service.CustomOAuth2UserService.OAuth2LoginEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthKafkaProducer {

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserRegistered(String email, String provider) {
        sendMessage("user-registered", String.format("{\"email\":\"%s\",\"provider\":\"%s\"}", email, provider));
    }

    public void sendOAuth2Login(String email, String provider) {
        sendMessage("oauth2-login", String.format("{\"email\":\"%s\",\"provider\":\"%s\"}", email, provider));
    }

    public void sendSubscriptionActivated(String email, String plan) {
        sendMessage("subscription-activated", String.format("{\"email\":\"%s\",\"plan\":\"%s\"}", email, plan));
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

    private void sendMessage(String topic, String payload) {
        try {
            if (kafkaTemplate != null) {
                kafkaTemplate.send(topic, payload);
                log.info("Sent async event to Kafka: topic={}, payload={}", topic, payload);
            } else {
                log.warn("Kafka Template is missing. Dropping event: topic={}, payload={}", topic, payload);
            }
        } catch (Exception e) {
            log.error("Failed to send Kafka event. Moving on without blocking main flow. Topic: {}, Error: {}", topic, e.getMessage());
        }
    }
}
