package com.hnv.elearning.common.otp;

public interface OtpService {
    String generateOtp();
    void verifyOtp(String key, String otp);
}
