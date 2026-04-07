package com.example.SmartCV.modules.payment.service;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentServiceFactory {

    private final Map<PaymentProvider, PaymentService> services;

    public PaymentServiceFactory(List<PaymentService> serviceList) {
        this.services = serviceList.stream()
            .collect(Collectors.toMap(PaymentService::getProvider, s -> s));
    }

    public PaymentService get(PaymentProvider provider) {
        return services.get(provider);
    }
}
