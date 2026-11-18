package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;
import com.epdcl.webdisplay.dto.HelpdeskEmployeeDto;
import com.epdcl.webdisplay.dto.TicketConversationMessageDto;

import java.util.List;
import java.util.Map;

public interface SupportTicketService {

    SupportTicketDto createTicket(Long customerId, SupportTicketDto dto);

    List<SupportTicketDto> getCustomerTickets(Long customerId);

    List<TicketConversationMessageDto> getCustomerConversation(
            Long customerId, Long ticketId);

    TicketConversationMessageDto addCustomerReply(
            Long customerId, Long ticketId, String message);

    List<SupportTicketDto> getAllTickets(Long userId);

    SupportTicketDto updateTicket(
            Long ticketId, Long userId, String status, String remarks);

    SupportTicketDto assignTicket(
            Long ticketId, Long currentUserId, Long employeeId);

    SupportTicketDto submitForReview(
            Long ticketId, Long userId, String remarks);

    SupportTicketDto reviewTicket(
            Long ticketId,
            Long managerUserId,
            String decision,
            String remarks,
            Long newEmployeeId);

    SupportTicketDto sendCustomerUpdate(
            Long ticketId, Long userId, String message);

    List<SupportTicketDto> getMyAssignedTickets(Long userId);

    TicketRemarkDto addRemark(
            Long ticketId, Long userId, String remarks);

    List<TicketRemarkDto> getTicketRemarks(
            Long ticketId, Long userId);

    Map<String, Long> getTicketStatistics(Long userId);

    List<SupportTicketDto> filterTickets(
            Long userId, String status, String priority);

    List<HelpdeskEmployeeDto> getHelpdeskEmployees(Long userId);

    SupportTicketDto endConversation(Long ticketId, Long userId);
}