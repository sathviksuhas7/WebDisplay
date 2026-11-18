package com.epdcl.webdisplay.dto;

import java.time.LocalDateTime;

public class SupportTicketDto {

    private Long ticketId;
    private Long customerId;

    private String category;
    private String subject;
    private String description;
    private String priority;
    private String status;

    private String resolutionRemarks;
    private String customerRemarks;
    private String employeeUpdates;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime slaDeadline;

    private boolean overdue;

    private boolean escalated;
    private LocalDateTime escalatedAt;

    private Long assignedEmployeeId;
    private String assignedEmployeeName;

    private boolean conversationEnded;
    private LocalDateTime conversationEndedAt;
    private String conversationEndedBy;

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolutionRemarks() {
        return resolutionRemarks;
    }

    public void setResolutionRemarks(String resolutionRemarks) {
        this.resolutionRemarks = resolutionRemarks;
    }

    public String getCustomerRemarks() {
        return customerRemarks;
    }

    public void setCustomerRemarks(String customerRemarks) {
        this.customerRemarks = customerRemarks;
    }

    public String getEmployeeUpdates() {
        return employeeUpdates;
    }

    public void setEmployeeUpdates(String employeeUpdates) {
        this.employeeUpdates = employeeUpdates;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public Long getAssignedEmployeeId() {
        return assignedEmployeeId;
    }

    public void setAssignedEmployeeId(Long assignedEmployeeId) {
        this.assignedEmployeeId = assignedEmployeeId;
    }

    public String getAssignedEmployeeName() {
        return assignedEmployeeName;
    }

    public void setAssignedEmployeeName(String assignedEmployeeName) {
        this.assignedEmployeeName = assignedEmployeeName;
    }

    public boolean isConversationEnded() {
        return conversationEnded;
    }

    public void setConversationEnded(boolean conversationEnded) {
        this.conversationEnded = conversationEnded;
    }

    public LocalDateTime getConversationEndedAt() {
        return conversationEndedAt;
    }

    public void setConversationEndedAt(LocalDateTime conversationEndedAt) {
        this.conversationEndedAt = conversationEndedAt;
    }

    public String getConversationEndedBy() {
        return conversationEndedBy;
    }

    public void setConversationEndedBy(String conversationEndedBy) {
        this.conversationEndedBy = conversationEndedBy;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public LocalDateTime getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(LocalDateTime escalatedAt) {
        this.escalatedAt = escalatedAt;
    }
}