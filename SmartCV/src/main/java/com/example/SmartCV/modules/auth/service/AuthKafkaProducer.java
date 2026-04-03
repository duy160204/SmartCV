package com.example.SmartCV.modules.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
