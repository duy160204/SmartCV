package com.example.SmartCV.modules.payment.service;

import java.util.Map;

public interface PaymentCallbackService {

    /**
     * Callback khi user được redirect về (Return URL)
     */
    void handleVNPayReturn(Map<String, String> params);

    /**
     * IPN server-to-server từ VNPay
     * @return true nếu xử lý thành công
     */
    boolean handleVNPayIpn(Map<String, String> params);
}
