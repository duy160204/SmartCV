package com.example.SmartCV.modules.subscription.domain;

public enum SubscriptionChangeType {
    ADMIN_UPDATE,      // admin nâng/hạ gói
    PAYMENT_SUCCESS,   // user mua gói thành công
    PAYMENT_FAILED,    // thanh toán fail
    EXPIRED,           // hết hạn tự động
    DOWNGRADE,         // hệ thống hạ gói
    SYSTEM_INIT        // hệ thống gán FREE ban đầu
}
