package com.example.SmartCV.modules.payment.service;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentCallbackService {

    /**
     * Callback khi user được redirect về (Return URL).
     * Nhận HttpServletRequest để đọc raw query string — không dùng Map đã decode.
     */
    void handleVNPayReturn(HttpServletRequest request);

    /**
     * IPN server-to-server từ VNPay.
     * Nhận HttpServletRequest để đọc raw query string — không dùng Map đã decode.
     * @return true nếu xử lý thành công
     */
    boolean handleVNPayIpn(HttpServletRequest request);
}
