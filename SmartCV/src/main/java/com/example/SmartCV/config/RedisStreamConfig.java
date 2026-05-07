package com.example.SmartCV.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

@Configuration
public class RedisStreamConfig {

    public static final String STREAM_AI_REQUEST = "stream:ai-request";
    public static final String STREAM_AUTH_EVENT = "stream:auth-event";

    @Bean
    public StreamMessageListenerContainer<String, org.springframework.data.redis.connection.stream.MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, org.springframework.data.redis.connection.stream.MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(100))
                        .build();

        return StreamMessageListenerContainer.create(connectionFactory, options);
    }
}
