package com.example.SmartCV.modules.payment.domain;

public enum PaymentProvider {

    /**
     * Thanh toán nội địa VN
     */
    VNPAY,

    /**
     * Thanh toán quốc tế
     */
    STRIPE,

    /**
     * Có thể mở rộng sau
     */
    PAYPAL
}
