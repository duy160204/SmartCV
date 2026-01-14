package com.example.SmartCV.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePaymentResponse {

    private String paymentUrl;
    private String transactionCode;
}
