package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.audit.AuditService;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.CustomerAddressChangeRequest;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.CustomerAddressChangeRequestRepository;
import com.epdcl.webdisplay.repo.CustomerRepository;
import com.epdcl.webdisplay.repo.ProfileOtpChallengeRepository;
import com.epdcl.webdisplay.entity.ProfileOtpChallenge;
import com.epdcl.webdisplay.service.impl.ProfileOtpService;
import com.epdcl.webdisplay.service.security.DocumentSecurityService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;

@RestController
@RequestMapping("/api")
public class CustomerAddressChangeController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final CustomerRepository customerRepository;
    private final CustomerAddressChangeRequestRepository requestRepository;
    private final ProfileOtpService otpService;
    private final ProfileOtpChallengeRepository otpRepository;
    private final AuditService auditService;
    private final Path uploadDirectory;
    private final DocumentSecurityService documentSecurityService;

    public CustomerAddressChangeController(
            CustomerRepository customerRepository,
            CustomerAddressChangeRequestRepository requestRepository,
            ProfileOtpService otpService,
            ProfileOtpChallengeRepository otpRepository,
            AuditService auditService,
            DocumentSecurityService documentSecurityService,
            @Value("${app.document.upload-dir}") String uploadDirectory) {

        this.documentSecurityService = documentSecurityService;

        this.customerRepository = customerRepository;
        this.requestRepository = requestRepository;
        this.otpService = otpService;
        this.otpRepository = otpRepository;
        this.auditService = auditService;
        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath().normalize().resolve("address-proofs")
                .normalize();
    }

    private User loggedInUser(HttpSession session) {
        Object value = session.getAttribute("loggedInUser");
        if (!(value instanceof User user)) {
            throw new IllegalArgumentException("Please log in again.");
        }
        return user;
    }

    private Customer customerFor(User user) {
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer profile not found."));
    }

    private void requireBankManager(User user) {
        if (user.getRole() != Role.BANK_MANAGER) {
            throw new SecurityException(
                    "Only Bank Managers can review address change requests.");
        }
    }

    @PostMapping("/customer/profile/address/request-otp")
    public ResponseEntity<?> requestAddressOtp(HttpSession session) {
        User user = loggedInUser(session);
        Customer customer = customerFor(user);

        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            throw new IllegalArgumentException(
                    "A registered phone number is required for address verification.");
        }

        String challengeId = otpService.generateOtp(
                user.getId(),
                "ADDRESS_CHANGE",
                "SMS",
                customer.getPhone());

        return ResponseEntity.ok(Map.of(
                "message", "Address verification OTP generated.",
                "challengeId", challengeId));
    }

    @PostMapping("/customer/profile/address/request")
    public ResponseEntity<?> submitAddressRequest(
            @RequestParam("requestedAddress") String requestedAddress,
            @RequestParam("challengeId") String challengeId,
            @RequestParam("otp") String otp,
            @RequestParam("proof") MultipartFile proof,
            HttpSession session,
            HttpServletRequest httpRequest) throws IOException {

        User user = loggedInUser(session);
        Customer customer = customerFor(user);

        String address = requestedAddress == null
                ? "" : requestedAddress.trim();

        if (address.isBlank() || address.length() > 1000) {
            throw new IllegalArgumentException(
                    "Enter a valid address (maximum 1000 characters).");
        }

        // The challenge must belong to this user and specifically verify
        // the currently registered phone for an address change.
        ProfileOtpChallenge challenge = otpRepository
                .findByChallengeId(challengeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Address OTP challenge not found. Request a new OTP."));

        if (!user.getId().equals(challenge.getUserId())
                || !"ADDRESS_CHANGE".equals(challenge.getPurpose())
                || !"SMS".equals(challenge.getChannel())
                || !customer.getPhone().equals(challenge.getDestination())) {
            throw new IllegalArgumentException(
                    "This OTP was not issued for your registered phone address verification.");
        }

        // Check the proof file first (format, structure, antivirus) so a bad
        // file does not use up the customer's OTP.
        validateProof(proof);
        DocumentSecurityService.ValidatedDocument validated =
                documentSecurityService.validateAndScan(proof);
        String extension = switch (validated.contentType()) {
            case "application/pdf" -> "pdf";
            case "image/png" -> "png";
            default -> "jpg";
        };

        otpService.verifyOtp(user.getId(), challengeId, otp);
        Files.createDirectories(uploadDirectory);

        String storedName = UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(storedName).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid proof file.");
        }

        Files.write(destination, validated.bytes(),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        CustomerAddressChangeRequest change =
                new CustomerAddressChangeRequest();

        change.setCustomerId(customer.getCustomerId());
        change.setRequestedAddress(address);
        change.setProofFileName(safeOriginalName(proof.getOriginalFilename()));
        change.setProofStoragePath(destination.toString());
        change.setStatus("PENDING");

        requestRepository.save(change);

        auditService.recordActivity(
                user,
                "CUSTOMER_ADDRESS_CHANGE_REQUESTED",
                "Customer submitted an address change request for review.",
                httpRequest);

        return ResponseEntity.ok(Map.of(
                "message", "Address change request submitted for bank approval.",
                "status", "PENDING"));
    }

    @GetMapping("/customer/profile/address/requests")
    public ResponseEntity<?> getMyAddressRequests(HttpSession session) {
        User user = loggedInUser(session);
        Customer customer = customerFor(user);

        List<Map<String, Object>> result =
                requestRepository.findByCustomerIdOrderByCreatedAtDesc(
                        customer.getCustomerId())
                        .stream()
                        .map(this::toCustomerMap)
                        .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/id-documents/manager/address-change/pending")
    public ResponseEntity<?> getPendingAddressRequests(HttpSession session) {
        User manager = loggedInUser(session);
        requireBankManager(manager);

        List<Map<String, Object>> result =
                requestRepository.findByStatusOrderByCreatedAtAsc("PENDING")
                        .stream()
                        .map(this::toManagerMap)
                        .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/id-documents/manager/address-change/{id}/proof")
    public ResponseEntity<Resource> viewAddressProof(
            @PathVariable Long id,
            HttpSession session) throws IOException {

        User manager = loggedInUser(session);
        requireBankManager(manager);

        CustomerAddressChangeRequest change =
                requestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Address request not found."));

        Path file = Paths.get(change.getProofStoragePath())
                .toAbsolutePath().normalize();

        if (!file.startsWith(uploadDirectory) || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Address proof file not found.");
        }

        String extension = extensionOf(file.getFileName().toString());
        MediaType mediaType = switch (extension) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + safeHeaderName(change.getProofFileName()) + "\"")
                .contentType(mediaType)
                .body(resource);
    }

    @PostMapping("/id-documents/manager/address-change/{id}/approve")
    @Transactional
    public ResponseEntity<?> approveAddressRequest(
            @PathVariable Long id,
            HttpSession session,
            HttpServletRequest httpRequest) {

        User manager = loggedInUser(session);
        requireBankManager(manager);

        CustomerAddressChangeRequest change =
                requestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Address request not found."));

        if (!"PENDING".equalsIgnoreCase(change.getStatus())) {
            throw new IllegalStateException("This request has already been reviewed.");
        }

        Customer customer = customerRepository.findById(change.getCustomerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer profile not found."));

        customer.setAddress(change.getRequestedAddress());
        customerRepository.save(customer);

        change.setStatus("APPROVED");
        change.setReviewedBy(manager.getId());
        change.setReviewedAt(LocalDateTime.now());
        change.setReviewReason("Approved by Bank Manager.");
        requestRepository.save(change);

        auditService.recordActivity(
                manager,
                "CUSTOMER_ADDRESS_CHANGE_APPROVED",
                "Bank Manager approved address change request ID " + id + ".",
                httpRequest);

        return ResponseEntity.ok(Map.of(
                "message", "Address change approved. Customer address updated."));
    }

    @PostMapping("/id-documents/manager/address-change/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectAddressRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session,
            HttpServletRequest httpRequest) {

        User manager = loggedInUser(session);
        requireBankManager(manager);

        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()
                || reason.trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "Enter a rejection reason (maximum 1000 characters).");
        }

        CustomerAddressChangeRequest change =
                requestRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Address request not found."));

        if (!"PENDING".equalsIgnoreCase(change.getStatus())) {
            throw new IllegalStateException("This request has already been reviewed.");
        }

        change.setStatus("REJECTED");
        change.setReviewReason(reason.trim());
        change.setReviewedBy(manager.getId());
        change.setReviewedAt(LocalDateTime.now());
        requestRepository.save(change);

        auditService.recordActivity(
                manager,
                "CUSTOMER_ADDRESS_CHANGE_REJECTED",
                "Bank Manager rejected address change request ID " + id + ".",
                httpRequest);

        return ResponseEntity.ok(Map.of(
                "message", "Address change request rejected. Existing address was not changed."));
    }

    private String validateProof(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload an address proof document.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Address proof must not exceed 5 MB.");
        }

        String extension = extensionOf(file.getOriginalFilename());
        byte[] header;
        try (var input = file.getInputStream()) {
            header = input.readNBytes(8);
        }

        boolean valid = switch (extension) {
            case "pdf" -> header.length >= 5
                    && header[0] == '%' && header[1] == 'P'
                    && header[2] == 'D' && header[3] == 'F' && header[4] == '-';
            case "jpg", "jpeg" -> header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;
            case "png" -> header.length >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                    && header[4] == 13 && header[5] == 10
                    && header[6] == 26 && header[7] == 10;
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "File content does not match PDF, JPG or PNG format.");
        }
        return extension;
    }

    private String extensionOf(String name) {
        if (name == null || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);
    }

    private String safeOriginalName(String name) {
        if (name == null || name.isBlank()) return "address-proof";
        return Paths.get(name).getFileName().toString()
                .replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String safeHeaderName(String name) {
        return safeOriginalName(name).replace("\"", "");
    }

    private Map<String, Object> toCustomerMap(
            CustomerAddressChangeRequest change) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", change.getId());
        item.put("requestedAddress", change.getRequestedAddress());
        item.put("status", change.getStatus());
        item.put("reviewReason", change.getReviewReason());
        item.put("createdAt", change.getCreatedAt());
        item.put("reviewedAt", change.getReviewedAt());
        return item;
    }

    private Map<String, Object> toManagerMap(
            CustomerAddressChangeRequest change) {
        Map<String, Object> item = toCustomerMap(change);
        Customer customer = customerRepository.findById(change.getCustomerId())
                .orElse(null);
        item.put("customerId", change.getCustomerId());
        item.put("customerName",
                customer == null ? "Unknown customer" : customer.getCustomerName());
        item.put("currentAddress",
                customer == null ? "" : customer.getAddress());
        item.put("proofFileName", change.getProofFileName());
        return item;
    }
}
