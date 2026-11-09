
package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;

import java.util.List;
import java.util.Map;

public interface SupportTicketService {

    SupportTicketDto createTicket(Long customerId, SupportTicketDto dto);

    List<SupportTicketDto> getCustomerTickets(Long customerId);

    List<SupportTicketDto> getAllTickets();

    SupportTicketDto updateTicket(
            Long ticketId, Long userId, String status, String remarks);

    SupportTicketDto assignTicket(Long ticketId, Long employeeId);

    List<SupportTicketDto> getMyAssignedTickets(Long userId);

    TicketRemarkDto addRemark(Long ticketId, Long userId, String remarks);

    List<TicketRemarkDto> getTicketRemarks(Long ticketId, Long userId);

    Map<String, Long> getTicketStatistics();

    List<SupportTicketDto> filterTickets(String status, String priority);
}
