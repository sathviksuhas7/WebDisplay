package com.epdcl.webdisplay.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_id_documents")
public class CustomerIdDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus = "PENDING";

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "uploaded_by_user_id")
    private Long uploadedByUserId;

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Long uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String name) { this.originalFileName = name; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String name) { this.storedFileName = name; }

    public String getContentType() { return contentType; }
    public void setContentType(String type) { this.contentType = type; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String path) { this.filePath = path; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String status) {
        this.verificationStatus = status;
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String reason) {
        this.rejectionReason = reason;
    }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}