package com.example.SmartCV.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.context.annotation.Profile;

@Profile("prod")
@Configuration
public class KafkaConfig {
    public static final String TOPIC_AI_WORKER = "ai-request";
    public static final String TOPIC_PDF_WORKER = "pdf-request";
    public static final String TOPIC_PAYMENT_REQ = "payment-request";
    public static final String TOPIC_PAYMENT_DLQ = "payment-dlq";

    @Bean public NewTopic aiWorkerTopic() { return TopicBuilder.name(TOPIC_AI_WORKER).partitions(5).replicas(1).build(); }
    @Bean public NewTopic pdfWorkerTopic() { return TopicBuilder.name(TOPIC_PDF_WORKER).partitions(3).replicas(1).build(); }
    @Bean public NewTopic paymentTopic() { return TopicBuilder.name(TOPIC_PAYMENT_REQ).partitions(3).replicas(1).build(); }
    @Bean public NewTopic paymentDlq() { return TopicBuilder.name(TOPIC_PAYMENT_DLQ).partitions(1).replicas(1).build(); }
}
