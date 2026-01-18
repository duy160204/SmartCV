package com.example.SmartCV.modules.payment.dto;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

    private String planCode;
    private PaymentProvider provider; // VNPAY / STRIPE
}
