package com.epdcl.webdisplay.service.impl;

public interface OtpDeliveryService {

    void deliverOtp(String destination, String otp, OtpPurpose purpose);
}