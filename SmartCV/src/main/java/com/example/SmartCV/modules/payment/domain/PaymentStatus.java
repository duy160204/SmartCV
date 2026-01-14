package com.example.SmartCV.modules.payment.domain;

public enum PaymentStatus {

    /**
     * Khởi tạo giao dịch (chưa redirect)
     */
    INIT,

    /**
     * Đã redirect sang cổng thanh toán
     */
    PENDING,

    /**
     * Thanh toán thành công (CHỈ TRẠNG THÁI NÀY MỚI UPDATE SUBSCRIPTION)
     */
    SUCCESS,

    /**
     * Thanh toán thất bại / bị huỷ
     */
    FAILED
}
