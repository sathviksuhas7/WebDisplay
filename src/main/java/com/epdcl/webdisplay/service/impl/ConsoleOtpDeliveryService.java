package com.epdcl.webdisplay.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod")
public class ConsoleOtpDeliveryService implements OtpDeliveryService {

    @Override
    public void deliverOtp(
            String destination,
            String otp,
            OtpPurpose purpose) {

        System.out.println();
        System.out.println("========== DEVELOPMENT OTP ==========");
        System.out.println("Purpose     : " + purpose);
        System.out.println("Destination : " + destination);
        System.out.println("OTP         : " + otp);
        System.out.println("Expires in  : 5 minutes");
        System.out.println("=====================================");
        System.out.println();
    }
}