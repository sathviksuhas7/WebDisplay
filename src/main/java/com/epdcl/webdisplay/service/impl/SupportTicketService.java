package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;

import java.util.List;

public interface SupportTicketService {

    // Customer operations
    SupportTicketDto createTicket(Long customerId, SupportTicketDto dto);

    List<SupportTicketDto> getCustomerTickets(Long customerId);

    // Helpdesk operations
    List<SupportTicketDto> getAllTickets();

    SupportTicketDto updateTicket(
            Long ticketId, Long userId, String status, String remarks);

    SupportTicketDto assignTicket(Long ticketId, Long employeeId);

    List<SupportTicketDto> getMyAssignedTickets(Long userId);

    // Ticket investigation remarks
    TicketRemarkDto addRemark(
            Long ticketId, Long userId, String remarks);

    List<TicketRemarkDto> getTicketRemarks(
            Long ticketId, Long userId);
}