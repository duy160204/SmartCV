package com.example.SmartCV.modules.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String paymentUrl;
    private String clientSecret;
    private String provider;
    private String transactionCode;
}
