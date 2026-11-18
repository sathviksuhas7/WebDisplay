package com.epdcl.webdisplay.service.impl;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
public class OtpService {

    private static final String SESSION_KEY = "OTP_CHALLENGES";

    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000;
    private static final long RESEND_COOLDOWN_MS = 60 * 1000;

    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    private final OtpDeliveryService otpDeliveryService;

    public OtpService(OtpDeliveryService otpDeliveryService) {
        this.otpDeliveryService = otpDeliveryService;
    }

    public void sendOtp(
            HttpSession session,
            OtpPurpose purpose,
            String destination) {

        if (session == null || purpose == null
                || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "OTP details are required");
        }

        String normalizedDestination = destination.trim();

        Map<OtpPurpose, OtpChallenge> challenges =
                getChallenges(session);

        OtpChallenge previous = challenges.get(purpose);

        long now = System.currentTimeMillis();

        if (previous != null
                && now - previous.createdAt < RESEND_COOLDOWN_MS) {

            throw new IllegalStateException(
                    "Please wait before requesting another OTP");
        }

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000));

        String salt = generateSalt();

        OtpChallenge challenge = new OtpChallenge(
                normalizedDestination,
                salt,
                hashOtp(salt, otp),
                now,
                0
        );

        challenges.put(purpose, challenge);

        otpDeliveryService.deliverOtp(
                normalizedDestination,
                otp,
                purpose
        );
    }

    public void verifyOtp(
            HttpSession session,
            OtpPurpose purpose,
            String destination,
            String enteredOtp) {

        if (session == null || purpose == null
                || destination == null || enteredOtp == null) {
            throw new IllegalArgumentException(
                    "OTP verification details are required");
        }

        Map<OtpPurpose, OtpChallenge> challenges =
                getChallenges(session);

        OtpChallenge challenge = challenges.get(purpose);

        if (challenge == null) {
            throw new IllegalStateException(
                    "OTP not found. Please request a new OTP");
        }

        if (!challenge.destination.equals(destination.trim())) {
            throw new IllegalStateException(
                    "OTP destination does not match");
        }

        long now = System.currentTimeMillis();

        if (now - challenge.createdAt > OTP_EXPIRY_MS) {
            challenges.remove(purpose);

            throw new IllegalStateException(
                    "OTP has expired. Please request a new OTP");
        }

        if (challenge.attempts >= MAX_ATTEMPTS) {
            challenges.remove(purpose);

            throw new IllegalStateException(
                    "Maximum OTP attempts exceeded. Request a new OTP");
        }

        String enteredHash = hashOtp(
                challenge.salt,
                enteredOtp.trim());

        if (!MessageDigest.isEqual(
                challenge.otpHash.getBytes(StandardCharsets.UTF_8),
                enteredHash.getBytes(StandardCharsets.UTF_8))) {

            challenge.attempts++;

            if (challenge.attempts >= MAX_ATTEMPTS) {
                challenges.remove(purpose);

                throw new IllegalStateException(
                        "Maximum OTP attempts exceeded. Request a new OTP");
            }

            throw new IllegalArgumentException(
                    "Incorrect OTP. Attempts remaining: "
                            + (MAX_ATTEMPTS - challenge.attempts));
        }

        challenges.remove(purpose);
    }

    private Map<OtpPurpose, OtpChallenge> getChallenges(
            HttpSession session) {

        @SuppressWarnings("unchecked")
        Map<OtpPurpose, OtpChallenge> challenges =
                (Map<OtpPurpose, OtpChallenge>)
                        session.getAttribute(SESSION_KEY);

        if (challenges == null) {
            challenges = new EnumMap<>(OtpPurpose.class);
            session.setAttribute(SESSION_KEY, challenges);
        }

        return challenges;
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashOtp(String salt, String otp) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    (salt + ":" + otp)
                            .getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to process OTP", e);
        }
    }

    private static class OtpChallenge implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String destination;
        private final String salt;
        private final String otpHash;
        private final long createdAt;
        private int attempts;

        private OtpChallenge(
                String destination,
                String salt,
                String otpHash,
                long createdAt,
                int attempts) {

            this.destination = destination;
            this.salt = salt;
            this.otpHash = otpHash;
            this.createdAt = createdAt;
            this.attempts = attempts;
        }
    }
}