package com.hnv.elearning.feature.order.enums;

public enum OrderStatus {
    PENDING,          // Chờ quét VietQR
    PARTIALLY_PAID,   // Đã chuyển một phần tiền (thiếu)
    PAID,             // Đã thanh toán đủ -> Kích hoạt khóa học
    CANCELLED,        // Khách chủ động hủy
    EXPIRED
}
