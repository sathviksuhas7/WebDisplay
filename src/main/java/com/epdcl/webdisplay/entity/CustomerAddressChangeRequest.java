package com.epdcl.webdisplay.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_address_change_requests")
public class CustomerAddressChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "requested_address", nullable = false, length = 1000)
    private String requestedAddress;

    @Column(name = "proof_file_name", nullable = false)
    private String proofFileName;

    @Column(name = "proof_storage_path", nullable = false, length = 1000)
    private String proofStoragePath;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "review_reason", length = 1000)
    private String reviewReason;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public Long getId() { return id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getRequestedAddress() { return requestedAddress; }
    public void setRequestedAddress(String requestedAddress) {
        this.requestedAddress = requestedAddress;
    }

    public String getProofFileName() { return proofFileName; }
    public void setProofFileName(String proofFileName) {
        this.proofFileName = proofFileName;
    }

    public String getProofStoragePath() { return proofStoragePath; }
    public void setProofStoragePath(String proofStoragePath) {
        this.proofStoragePath = proofStoragePath;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewReason() { return reviewReason; }
    public void setReviewReason(String reviewReason) {
        this.reviewReason = reviewReason;
    }

    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}