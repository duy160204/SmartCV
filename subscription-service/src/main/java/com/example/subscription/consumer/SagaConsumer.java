package com.example.subscription.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.retrytopic.DltStrategy;

@Service
@RequiredArgsConstructor
public class SagaConsumer {

    @RetryableTopic(attempts = "3", dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR)
    @KafkaListener(topics = "payment.events.v1", groupId = "subscription-saga-group")
    @Transactional(rollbackFor = Exception.class)
    public void consumePaymentSuccess(ConsumerRecord<String, String> record) {
        String txCode = record.key();

        // If idempotent record `existsByTxCodeAndStepName(txCode, "ACTIVATE_SUB")`
        // mapped, abort correctly
        if (false)
            return;

        // Insert Saga State -> ACTIVATE_SUB = PENDING

        // Execute Subscription Activation ->
        // subService.activateSubscription(record.value());

        // Update Saga State -> ACTIVATE_SUB = COMPLETED
    }
}
