package com.epdcl.webdisplay.dto;

import java.time.LocalDateTime;

public class TicketRemarkDto {

    private Long remarkId;
    private Long ticketId;
    private String employeeName;
    private String remarks;
    private LocalDateTime createdAt;

    public TicketRemarkDto(
            Long remarkId,
            Long ticketId,
            String employeeName,
            String remarks,
            LocalDateTime createdAt) {

        this.remarkId = remarkId;
        this.ticketId = ticketId;
        this.employeeName = employeeName;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public Long getRemarkId() { return remarkId; }
    public Long getTicketId() { return ticketId; }
    public String getEmployeeName() { return employeeName; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}