package com.example.SmartCV.modules.payment.dto;

import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.payment.domain.PaymentProvider;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {

    private PlanType plan;
    private Integer months;
    private PaymentProvider provider; // VNPAY / STRIPE
}
