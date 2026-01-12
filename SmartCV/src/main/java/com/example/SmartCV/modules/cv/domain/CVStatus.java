package com.example.SmartCV.modules.cv.domain;

public enum CVStatus {
    DRAFT,       // đang chỉnh sửa
    PUBLISHED,   // đã publish
    SHARED,      // đang được share
    ARCHIVED     // lưu trữ (sau này dùng)
, TEMPLATE_DELETED, TEMPLATE_LOCKED
}
