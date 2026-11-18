package com.epdcl.webdisplay.dto;

import java.time.LocalDateTime;

public class TicketConversationMessageDto {

    private Long messageId;
    private Long ticketId;
    private String senderType;
    private String senderName;
    private String message;
    private LocalDateTime createdAt;

    public TicketConversationMessageDto(
            Long messageId,
            Long ticketId,
            String senderType,
            String senderName,
            String message,
            LocalDateTime createdAt) {

        this.messageId = messageId;
        this.ticketId = ticketId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public String getSenderType() {
        return senderType;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}