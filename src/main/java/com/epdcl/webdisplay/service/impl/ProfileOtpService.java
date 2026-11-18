package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.ProfileOtpChallenge;
import com.epdcl.webdisplay.repo.ProfileOtpChallengeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProfileOtpService {

    private static final Logger log =
            LoggerFactory.getLogger(ProfileOtpService.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;

    private final ProfileOtpChallengeRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final boolean logOtp;

    public ProfileOtpService(
            ProfileOtpChallengeRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.profile-otp.log-code:false}") boolean logOtp) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.logOtp = logOtp;
    }

    /**
     * Creates an OTP challenge and returns its challenge ID.
     * OTP itself is never returned to the frontend.
     */
    @Transactional
    public String generateOtp(
            Long userId,
            String purpose,
            String channel,
            String destination) {

        if (userId == null || purpose == null || purpose.isBlank()
                || channel == null || destination == null
                || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid OTP request");
        }

        String normalizedChannel = channel.trim().toUpperCase();

        if (!normalizedChannel.equals("SMS")
                && !normalizedChannel.equals("EMAIL")) {
            throw new IllegalArgumentException(
                    "Unsupported OTP channel");
        }

        repository
                .findFirstByUserIdAndPurposeAndChannelOrderByIdDesc(
                        userId, purpose, normalizedChannel)
                .ifPresent(previous -> {
                    LocalDateTime cooldownEnd =
                            previous.getCreatedAt() == null
                                    ? LocalDateTime.now()
                                    : previous.getCreatedAt()
                                    .plusSeconds(RESEND_COOLDOWN_SECONDS);

                    if (LocalDateTime.now().isBefore(cooldownEnd)) {
                        throw new IllegalStateException(
                                "Please wait before requesting another OTP");
                    }
                });

        // Invalidate the previous challenge for this purpose and channel.
        repository.deleteByUserIdAndPurposeAndChannel(
                userId, purpose, normalizedChannel);

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        ProfileOtpChallenge challenge = new ProfileOtpChallenge();
        challenge.setChallengeId(UUID.randomUUID().toString());
        challenge.setUserId(userId);
        challenge.setPurpose(purpose.trim().toUpperCase());
        challenge.setChannel(normalizedChannel);
        challenge.setDestination(destination.trim());
        challenge.setOtpHash(passwordEncoder.encode(otp));
        challenge.setExpiresAt(
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        challenge.setAttempts(0);

        repository.save(challenge);

        if (logOtp) {
            log.warn(
                    "DEVELOPMENT OTP | userId={} | purpose={} | channel={} | destination={} | code={}",
                    userId, purpose, normalizedChannel, destination, otp);
        } else {
            log.info(
                    "OTP challenge generated | userId={} | purpose={} | channel={}",
                    userId, purpose, normalizedChannel);
        }

        return challenge.getChallengeId();
    }

    /**
     * Verifies an OTP. Returns true only when the code is correct.
     */
    @Transactional(noRollbackFor = {IllegalArgumentException.class, IllegalStateException.class})
    public boolean verifyOtp(
            Long userId,
            String challengeId,
            String submittedOtp) {

        if (userId == null || challengeId == null
                || challengeId.isBlank()
                || submittedOtp == null
                || !submittedOtp.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "Enter a valid 6-digit OTP");
        }

        ProfileOtpChallenge challenge =
                repository.findByChallengeId(challengeId.trim())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "OTP challenge not found or expired"));

        if (!challenge.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Invalid OTP challenge");
        }

        if (challenge.getVerifiedAt() != null) {
            throw new IllegalStateException(
                    "This OTP has already been used");
        }

        if (LocalDateTime.now().isAfter(challenge.getExpiresAt())) {
            repository.delete(challenge);
            throw new IllegalStateException(
                    "OTP expired. Please request a new OTP");
        }

        if (challenge.getAttempts() >= MAX_ATTEMPTS) {
            repository.delete(challenge);
            throw new IllegalStateException(
                    "Maximum OTP attempts exceeded. Request a new OTP");
        }

        if (!passwordEncoder.matches(
                submittedOtp, challenge.getOtpHash())) {

            challenge.setAttempts(challenge.getAttempts() + 1);
            repository.save(challenge);

            if (challenge.getAttempts() >= MAX_ATTEMPTS) {
                repository.delete(challenge);
                throw new IllegalStateException(
                        "Maximum OTP attempts exceeded. Request a new OTP");
            }

            throw new IllegalArgumentException(
                    "Incorrect OTP. Attempts remaining: "
                            + (MAX_ATTEMPTS - challenge.getAttempts()));
        }

        challenge.setVerifiedAt(LocalDateTime.now());
        repository.save(challenge);

        return true;
    }
}