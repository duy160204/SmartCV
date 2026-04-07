package com.example.SmartCV.modules.payment.service;

import com.example.SmartCV.modules.admin.service.AdminSubscriptionRequestService;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import java.time.LocalDateTime;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.dto.PaymentResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService implements PaymentService {

    @Value("${stripe.api-key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    private final PaymentTransactionRepository paymentRepository;
    private final AdminSubscriptionRequestService adminSubscriptionRequestService;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            log.warn("Stripe API key is missing. StripePaymentService will be disabled.");
            return;
        }
        Stripe.apiKey = this.stripeApiKey;
    }

    @Override
    public boolean isEnabled() {
        return stripeApiKey != null && !stripeApiKey.isBlank();
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentResponse createPayment(PaymentTransaction tx) {
        String clientSecret = createPaymentIntent(tx);
        return PaymentResponse.builder()
                .clientSecret(clientSecret)
                .provider(getProvider().name())
                .transactionCode(tx.getTransactionCode())
                .build();
    }

    @Transactional
    public String createPaymentIntent(PaymentTransaction tx) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    // Stripe requires amount in smallest currency unit (e.g. cents)
                    .setAmount(tx.getAmount())
                    .setCurrency("vnd") // Adjust to match Stripe support for VND, mostly VND maps directly as an integer.
                    .putMetadata("transactionCode", tx.getTransactionCode())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            
            // Save Payment Intent ID to Transaction Entity mapping
            tx.setExternalId(intent.getId());
            paymentRepository.save(tx);
            
            return intent.getClientSecret();
        } catch (Exception e) {
            log.error("[STRIPE] Failed to create PaymentIntent", e);
            throw new RuntimeException("Failed to generate Stripe payment.", e);
        }
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                    if (paymentIntent != null) {
                        handlePaymentIntentSucceeded(paymentIntent);
                    }
                    break;
                case "payment_intent.payment_failed":
                    PaymentIntent failedIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                    if (failedIntent != null) {
                        handlePaymentIntentFailed(failedIntent);
                    }
                    break;
                default:
                    log.info("[STRIPE] Unhandled event type: " + event.getType());
            }

        } catch (Exception e) {
            log.error("[STRIPE] Webhook processing failed.", e);
            throw new RuntimeException("Stripe webhook verification failed.", e);
        }
    }

    private void handlePaymentIntentSucceeded(PaymentIntent intent) {
        String transactionCode = intent.getMetadata().get("transactionCode");
        log.info("[STRIPE][SUCCESS] Resolving Payment for transactionCode={}", transactionCode);
        
        PaymentTransaction tx = paymentRepository.findByTransactionCode(transactionCode)
            .orElseThrow(() -> new RuntimeException("Transaction lookup failed for Stripe Webhook"));
            
        if (tx.getStatus() == PaymentStatus.SUCCESS) {
             log.warn("[STRIPE] Transaction already marked SUCCESS.");
             return; // Idempotent 
        }

        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setPaidAt(LocalDateTime.now());
        paymentRepository.save(tx);
        
        // Pass strictly to preview flow bypassing activation 
        adminSubscriptionRequestService.createFromPaymentSuccess(tx);
    }

    private void handlePaymentIntentFailed(PaymentIntent intent) {
        String transactionCode = intent.getMetadata().get("transactionCode");
        PaymentTransaction tx = paymentRepository.findByTransactionCode(transactionCode).orElse(null);
        if (tx != null) {
             tx.setStatus(PaymentStatus.FAILED);
             paymentRepository.save(tx);
        }
    }
}
