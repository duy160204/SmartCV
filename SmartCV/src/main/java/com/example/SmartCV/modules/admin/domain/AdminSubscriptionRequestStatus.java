package com.example.SmartCV.modules.admin.domain;

public enum AdminSubscriptionRequestStatus {

    PENDING,     // payment SUCCESS, chờ admin
    PREVIEWED,   // admin đã preview
    CONFIRMED,   // admin đã confirm → sub active
    REJECTED     // admin từ chối (refund / manual)
}
