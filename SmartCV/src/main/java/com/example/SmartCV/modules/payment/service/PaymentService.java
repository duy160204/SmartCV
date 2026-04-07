package com.example.SmartCV.modules.payment.service;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.dto.PaymentResponse;

public interface PaymentService {
    PaymentProvider getProvider();
    boolean isEnabled();
    PaymentResponse createPayment(PaymentTransaction tx);
}
