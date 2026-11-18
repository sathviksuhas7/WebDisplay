
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.ProfileOtpChallenge;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.CustomerRepository;
import com.epdcl.webdisplay.repo.ProfileOtpChallengeRepository;
import com.epdcl.webdisplay.repo.UserRepository;
import com.epdcl.webdisplay.service.impl.ProfileOtpService;
import com.epdcl.webdisplay.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/customer/profile")
public class CustomerProfileVerificationController {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProfileOtpChallengeRepository otpRepository;
    private final ProfileOtpService otpService;
    private final AuditService auditService;

    public CustomerProfileVerificationController(
            CustomerRepository customerRepository,
            UserRepository userRepository,
            ProfileOtpChallengeRepository otpRepository,
            ProfileOtpService otpService,
            AuditService auditService) {

        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.otpService = otpService;
        this.auditService = auditService;
    }

    private User getLoggedInUser(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            throw new IllegalArgumentException("Please log in again");
        }

        return user;
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("[\\s().-]", "");
    }

    private boolean isValidPhone(String phone) {
        String normalized = normalizePhone(phone);
        return normalized != null
                && normalized.matches("^\\+?[1-9][0-9]{7,14}$");
    }

    private Customer getCustomer(Long userId) {
        return customerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer profile not found"));
    }

    private void validateChallenge(
            Long userId,
            String challengeId,
            String purpose,
            String channel,
            String destination) {

        ProfileOtpChallenge challenge = otpRepository
                .findByChallengeId(challengeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid OTP challenge"));

        if (!challenge.getUserId().equals(userId)
                || !purpose.equals(challenge.getPurpose())
                || !channel.equals(challenge.getChannel())
                || !destination.equalsIgnoreCase(challenge.getDestination())) {

            throw new IllegalArgumentException("OTP challenge does not match");
        }
    }

    // PHONE: Request OTPs for existing and new phone numbers
    @PostMapping("/phone/request-otp")
    public ResponseEntity<?> requestPhoneOtps(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);
        Customer customer = getCustomer(user.getId());

        String newPhone = normalizePhone(request.get("newPhone"));

        if (newPhone == null
                || !isValidPhone(newPhone)) {
            throw new IllegalArgumentException(
                    "Enter a valid phone number with 8 to 15 digits. Use + for the country code if needed.");
        }

        if (newPhone.equals(customer.getPhone())) {
            throw new IllegalArgumentException(
                    "New phone number must be different");
        }

        String oldChallenge = otpService.generateOtp(
                user.getId(), "PHONE_OLD", "SMS", customer.getPhone());

        String newChallenge = otpService.generateOtp(
                user.getId(), "PHONE_NEW", "SMS", newPhone);

        return ResponseEntity.ok(Map.of(
                "message", "OTP challenges created",
                "oldChallengeId", oldChallenge,
                "newChallengeId", newChallenge
        ));
    }

    // PHONE: Verify both OTPs and update phone
    @PostMapping("/phone/confirm")
    public ResponseEntity<?> confirmPhoneChange(
            @RequestBody Map<String, String> request,
            HttpSession session,
            HttpServletRequest httpRequest) {

        User user = getLoggedInUser(session);
        Customer customer = getCustomer(user.getId());

        String newPhone = normalizePhone(request.get("newPhone"));
        String oldChallengeId = request.get("oldChallengeId");
        String newChallengeId = request.get("newChallengeId");

        if (newPhone == null
                || !isValidPhone(newPhone)) {
            throw new IllegalArgumentException("Invalid new phone number");
        }

        validateChallenge(user.getId(), oldChallengeId,
                "PHONE_OLD", "SMS", customer.getPhone());

        validateChallenge(user.getId(), newChallengeId,
                "PHONE_NEW", "SMS", newPhone);

        otpService.verifyOtp(
                user.getId(), oldChallengeId, request.get("oldOtp"));

        otpService.verifyOtp(
                user.getId(), newChallengeId, request.get("newOtp"));

        customer.setPhone(newPhone);
        customerRepository.save(customer);
        auditService.recordActivity(user, "CUSTOMER_PHONE_UPDATED",
                "Customer updated registered phone number after OTP verification.", httpRequest);

        return ResponseEntity.ok(Map.of(
                "message", "Phone number updated successfully"
        ));
    }

    // EMAIL: Request OTPs for old email, new email and registered phone
    @PostMapping("/email/request-otp")
    public ResponseEntity<?> requestEmailOtps(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);
        Customer customer = getCustomer(user.getId());

        String newEmail = request.get("newEmail");

        if (newEmail == null
                || !Pattern.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                newEmail.trim())) {
            throw new IllegalArgumentException("Enter a valid email address");
        }

        newEmail = newEmail.trim().toLowerCase();

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException(
                    "New email must be different");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException(
                    "This email address is already registered");
        }

        String oldEmailChallenge = otpService.generateOtp(
                user.getId(), "EMAIL_OLD", "EMAIL", user.getEmail());

        String newEmailChallenge = otpService.generateOtp(
                user.getId(), "EMAIL_NEW", "EMAIL", newEmail);

        String phoneChallenge = otpService.generateOtp(
                user.getId(), "EMAIL_PHONE", "SMS", customer.getPhone());

        return ResponseEntity.ok(Map.of(
                "message", "OTP challenges created",
                "oldEmailChallengeId", oldEmailChallenge,
                "newEmailChallengeId", newEmailChallenge,
                "phoneChallengeId", phoneChallenge
        ));
    }

    // EMAIL: Verify all three OTPs and update email
    @PostMapping("/email/confirm")
    public ResponseEntity<?> confirmEmailChange(
            @RequestBody Map<String, String> request,
            HttpSession session,
            HttpServletRequest httpRequest) {

        User user = getLoggedInUser(session);
        Customer customer = getCustomer(user.getId());

        String newEmail = request.get("newEmail");

        if (newEmail == null) {
            throw new IllegalArgumentException("New email is required");
        }

        newEmail = newEmail.trim().toLowerCase();

        if (!Pattern.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                newEmail)) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException(
                    "This email address is already registered");
        }

        String oldId = request.get("oldEmailChallengeId");
        String newId = request.get("newEmailChallengeId");
        String phoneId = request.get("phoneChallengeId");

        validateChallenge(user.getId(), oldId,
                "EMAIL_OLD", "EMAIL", user.getEmail());

        validateChallenge(user.getId(), newId,
                "EMAIL_NEW", "EMAIL", newEmail);

        validateChallenge(user.getId(), phoneId,
                "EMAIL_PHONE", "SMS", customer.getPhone());

        otpService.verifyOtp(
                user.getId(), oldId, request.get("oldEmailOtp"));

        otpService.verifyOtp(
                user.getId(), newId, request.get("newEmailOtp"));

        otpService.verifyOtp(
                user.getId(), phoneId, request.get("phoneOtp"));

        user.setEmail(newEmail);
        userRepository.save(user);
        auditService.recordActivity(user, "CUSTOMER_EMAIL_UPDATED",
                "Customer updated registered email after OTP verification.", httpRequest);

        return ResponseEntity.ok(Map.of(
                "message", "Email address updated successfully"
        ));
    }
}