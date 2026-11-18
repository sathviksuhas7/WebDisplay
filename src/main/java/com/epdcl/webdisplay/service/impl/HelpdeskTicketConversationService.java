package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.TicketConversationMessageDto;
import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.SupportTicket;
import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.repo.EmployeeRepository;
import com.epdcl.webdisplay.repo.SupportTicketRepository;
import com.epdcl.webdisplay.repo.TicketConversationMessageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HelpdeskTicketConversationService {

    private static final String MANAGER = "HELPDESK_MANAGER";

    private final SupportTicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketConversationMessageRepository conversationRepository;

    public HelpdeskTicketConversationService(
            SupportTicketRepository ticketRepository,
            EmployeeRepository employeeRepository,
            TicketConversationMessageRepository conversationRepository) {

        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
        this.conversationRepository = conversationRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketConversationMessageDto> getConversation(
            Long userId, Long ticketId) {

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountException("Employee profile not found"));

        if (employee.getUser() == null
                || employee.getUser().getRole() != Role.HELPDESK) {

            throw new AccountException(
                    "Only Helpdesk users can view ticket conversations");
        }

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        boolean isManager =
                MANAGER.equalsIgnoreCase(employee.getDesignation());

        if (!isManager) {
            if (ticket.getAssignedEmployee() == null
                    || !ticket.getAssignedEmployee().getEmployeeId()
                    .equals(employee.getEmployeeId())) {

                throw new AccountException(
                        "This ticket is not assigned to you");
            }
        }

        return conversationRepository
                .findByTicketTicketIdOrderByCreatedAtAscMessageIdAsc(ticketId)
                .stream()
                .map(message -> new TicketConversationMessageDto(
                        message.getMessageId(),
                        ticketId,
                        message.getSenderType(),
                        message.getSenderName(),
                        message.getMessage(),
                        message.getCreatedAt()))
                .collect(Collectors.toList());
    }
}