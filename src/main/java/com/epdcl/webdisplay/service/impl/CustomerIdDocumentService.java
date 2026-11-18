package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.entity.*;
import com.epdcl.webdisplay.repo.*;
import com.epdcl.webdisplay.audit.annotation.Audited;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class CustomerIdDocumentService {

    private final CustomerIdDocumentRepository documentRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final Path uploadDirectory;


    public CustomerIdDocumentService(
            CustomerIdDocumentRepository documentRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            AccountService accountService,
            @Value("${app.document.upload-dir}") String uploadDir) {

        this.documentRepository = documentRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();


    }

    @Transactional(readOnly = true)
    public List<CustomerIdDocument> getAllApplicationsForEmployee(
            Long uploadedByUserId) {

        return documentRepository
                .findByUploadedByUserIdOrderByUploadedAtDesc(uploadedByUserId);
    }

    @Transactional
    public AccountDto createAccountWithDocument(
            Long customerId,
            String branchName,
            String district,
            MultipartFile file,
            Long uploadedByUserId) throws IOException {

        validateFile(file);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        AccountDto dto = new AccountDto();
        dto.setCustomerId(customerId);
        dto.setBranchName(branchName);
        dto.setDistrict(district);

        // Create using your existing account service and duplicate-account rule.
        AccountDto created = accountService.createAccount(dto);

        Account account = accountRepository
                .findByAccountNumber(created.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Created account not found"));

        // Account must remain inactive until admin approval.
        account.setActive(false);
        accountRepository.save(account);

        Files.createDirectories(uploadDirectory);

        String originalName = getSafeOriginalFileName(file);

        String extension = originalName.substring(
                originalName.lastIndexOf('.') + 1).toLowerCase();

        String storedName = UUID.randomUUID() + "." + extension;
        Path destination = uploadDirectory.resolve(storedName).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new RuntimeException("Invalid file path");
        }

        Files.copy(file.getInputStream(), destination,
                StandardCopyOption.REPLACE_EXISTING);

        CustomerIdDocument document = new CustomerIdDocument();
        document.setCustomer(customer);
        document.setAccount(account);
        document.setOriginalFileName(originalName);
        document.setStoredFileName(storedName);
        document.setContentType(file.getContentType());
        document.setFilePath(destination.toString());
        document.setVerificationStatus("PENDING");
        document.setUploadedByUserId(uploadedByUserId);

        documentRepository.save(document);

        return created;
    }


    @Transactional
    public void reuploadRejectedDocument(
            Long documentId,
            MultipartFile file,
            Long uploadedByUserId) throws IOException {

        validateFile(file);

        CustomerIdDocument document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document application not found"));

        // Employee can only update their own application.
        if (!java.util.Objects.equals(
                document.getUploadedByUserId(), uploadedByUserId)) {
            throw new RuntimeException(
                    "You are not authorized to re-upload this document.");
        }

        if (!"REJECTED".equalsIgnoreCase(
                document.getVerificationStatus())) {
            throw new RuntimeException(
                    "Only rejected documents can be re-uploaded.");
        }

        Files.createDirectories(uploadDirectory);

        String originalName = getSafeOriginalFileName(file);

        String extension = originalName.substring(
                originalName.lastIndexOf('.') + 1).toLowerCase();

        String storedName = UUID.randomUUID() + "." + extension;

        Path destination = uploadDirectory.resolve(storedName).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new RuntimeException("Invalid file path");
        }

        Files.copy(file.getInputStream(), destination,
                StandardCopyOption.REPLACE_EXISTING);

        // Update the EXISTING document record.
        document.setOriginalFileName(originalName);
        document.setStoredFileName(storedName);
        document.setContentType(file.getContentType());
        document.setFilePath(destination.toString());
        document.setVerificationStatus("PENDING");
        document.setRejectionReason(null);
        document.setReviewedAt(null);
        document.setUploadedAt(LocalDateTime.now());

        documentRepository.save(document);
    }


    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please upload an ID document");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size must not exceed 5 MB");
        }

        String name = file.getOriginalFilename();

        if (name == null || !name.matches("(?i).+\\.(pdf|jpg|jpeg|png)$")) {
            throw new RuntimeException("Only PDF, JPG, JPEG and PNG files are allowed");
        }

        String type = file.getContentType();

        if (type == null || !List.of(
                "application/pdf", "image/jpeg", "image/png"
        ).contains(type)) {
            throw new RuntimeException("Invalid document file type");
        }
    }

    public List<CustomerIdDocument> getPendingDocuments() {
        return documentRepository
                .findByVerificationStatusOrderByUploadedAtAsc("PENDING");
    }

    public Resource getDocumentFile(Long id) {

        CustomerIdDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Path path = Paths.get(document.getFilePath()).normalize();

        if (!path.startsWith(uploadDirectory) || !Files.exists(path)) {
            throw new RuntimeException("Document file not found");
        }

        return new FileSystemResource(path);
    }
    @Audited(
            action = "BANK_MANAGER_DOCUMENT_APPROVED",
            details = "Bank Manager approved customer ID document and activated the account"
    )
    @Transactional
    public void approveDocument(Long id) {
        CustomerIdDocument document = documentRepository
                .findByIdAndVerificationStatus(id, "PENDING")
                .orElseThrow(() -> new RuntimeException("Pending document not found"));

        document.setVerificationStatus("APPROVED");
        document.setReviewedAt(LocalDateTime.now());

        Account account = document.getAccount();
        account.setActive(true);

        accountRepository.save(account);
        documentRepository.save(document);
    }

    @Audited(
            action = "BANK_MANAGER_DOCUMENT_REJECTED",
            details = "Bank Manager rejected customer ID document"
    )
    @Transactional
    public void rejectDocument(Long id, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Please provide a rejection reason");
        }

        CustomerIdDocument document = documentRepository
                .findByIdAndVerificationStatus(id, "PENDING")
                .orElseThrow(() -> new RuntimeException("Pending document not found"));

        document.setVerificationStatus("REJECTED");
        document.setRejectionReason(reason.trim());
        document.setReviewedAt(LocalDateTime.now());

        // Account deliberately remains inactive.
        documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<CustomerIdDocument> getRejectedDocumentsForEmployee(
            Long uploadedByUserId) {

        return documentRepository
                .findByUploadedByUserIdAndVerificationStatusOrderByReviewedAtDesc(
                        uploadedByUserId, "REJECTED");
    }

    @Transactional(readOnly = true)
    public Page<CustomerIdDocument> searchEmployeeApplications(
            Long employeeId,
            String search,
            String status,
            Pageable pageable) {

        Specification<CustomerIdDocument> specification =
                (root, query, criteriaBuilder) -> {

                    List<Predicate> predicates = new ArrayList<>();

                    // Only applications created by the logged-in employee.
                    predicates.add(
                            criteriaBuilder.equal(
                                    root.get("uploadedByUserId"),
                                    employeeId
                            )
                    );

                    if (search != null && !search.isBlank()) {

                        String keyword = search.trim();

                        String likeKeyword =
                                "%" + keyword.toLowerCase() + "%";

                        Predicate nameMatch = criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("customer").get("customerName")
                                ),
                                likeKeyword
                        );

                        Predicate accountMatch = criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("account").get("accountNumber")
                                ),
                                likeKeyword
                        );

                        List<Predicate> searchPredicates = new ArrayList<>();

                        searchPredicates.add(nameMatch);
                        searchPredicates.add(accountMatch);

                        // Customer ID is numeric.
                        try {
                            Long customerId = Long.parseLong(keyword);

                            searchPredicates.add(
                                    criteriaBuilder.equal(
                                            root.get("customer").get("customerId"),
                                            customerId
                                    )
                            );

                        } catch (NumberFormatException ignored) {
                            // Name and account number searches still work.
                        }

                        predicates.add(
                                criteriaBuilder.or(
                                        searchPredicates.toArray(new Predicate[0])
                                )
                        );
                    }

                    if (status != null
                            && !status.isBlank()
                            && !"ALL".equalsIgnoreCase(status)) {

                        predicates.add(
                                criteriaBuilder.equal(
                                        criteriaBuilder.upper(
                                                root.get("verificationStatus")
                                        ),
                                        status.trim().toUpperCase()
                                )
                        );
                    }

                    return criteriaBuilder.and(
                            predicates.toArray(new Predicate[0])
                    );
                };

        return documentRepository.findAll(specification, pageable);
    }

    private String getSafeOriginalFileName(MultipartFile file) {

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new RuntimeException("Invalid document filename.");
        }

        return Paths.get(originalName).getFileName().toString();
    }


}