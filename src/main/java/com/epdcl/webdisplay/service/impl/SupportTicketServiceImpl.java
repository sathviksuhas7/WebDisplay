package com.epdcl.webdisplay.service.impl;


import com.epdcl.webdisplay.audit.annotation.Audited;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;
import com.epdcl.webdisplay.dto.HelpdeskEmployeeDto;
import com.epdcl.webdisplay.dto.TicketConversationMessageDto;

import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.SupportTicket;
import com.epdcl.webdisplay.entity.TicketRemark;
import com.epdcl.webdisplay.entity.TicketConversationMessage;

import com.epdcl.webdisplay.exception.AccountException;

import com.epdcl.webdisplay.repo.CustomerRepository;
import com.epdcl.webdisplay.repo.EmployeeRepository;
import com.epdcl.webdisplay.repo.SupportTicketRepository;
import com.epdcl.webdisplay.repo.TicketRemarkRepository;
import com.epdcl.webdisplay.repo.TicketConversationMessageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    private static final String MANAGER = "HELPDESK_MANAGER";

    private final TicketRemarkRepository remarkRepository;
    private final SupportTicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketConversationMessageRepository conversationRepository;

    public SupportTicketServiceImpl(
            SupportTicketRepository ticketRepository,
            CustomerRepository customerRepository,
            EmployeeRepository employeeRepository,
            TicketRemarkRepository remarkRepository,
            TicketConversationMessageRepository conversationRepository) {

        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.remarkRepository = remarkRepository;
        this.conversationRepository = conversationRepository;
    }

    // ---------------------------------------------------------
    // SLA
    // ---------------------------------------------------------

    private LocalDateTime calculateSlaDeadline(
            String priority, LocalDateTime createdAt) {

        return switch (priority.toUpperCase()) {
            case "URGENT" -> createdAt.plusHours(1);
            case "HIGH" -> createdAt.plusHours(4);
            //case "MEDIUM" -> createdAt.plusHours(24);
            case "LOW" -> createdAt.plusHours(48);
            default -> createdAt.plusHours(24);
        };
    }

    private boolean isTicketOverdue(SupportTicket ticket) {

        String status = ticket.getStatus();

        if (status == null
                || "CLOSED".equalsIgnoreCase(status)
                || "RESOLVED".equalsIgnoreCase(status)) {
            return false;
        }

        return ticket.getSlaDeadline() != null
                && LocalDateTime.now().isAfter(ticket.getSlaDeadline());
    }

    // ---------------------------------------------------------
    // DTO MAPPING
    // ---------------------------------------------------------

    private SupportTicketDto mapToDto(SupportTicket ticket) {

        SupportTicketDto dto = new SupportTicketDto();

        dto.setTicketId(ticket.getTicketId());
        dto.setCustomerId(ticket.getCustomer().getCustomerId());
        dto.setCategory(ticket.getCategory());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setStatus(ticket.getStatus());
        dto.setCustomerRemarks(ticket.getCustomerRemarks());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setSlaDeadline(ticket.getSlaDeadline());
        dto.setOverdue(isTicketOverdue(ticket));
        dto.setEscalated(ticket.isEscalated());
        dto.setEscalatedAt(ticket.getEscalatedAt());
        dto.setResolutionRemarks(ticket.getResolutionRemarks());
        dto.setConversationEnded(ticket.isConversationEnded());
        dto.setConversationEndedAt(ticket.getConversationEndedAt());
        dto.setConversationEndedBy(ticket.getConversationEndedBy());

        if (ticket.getAssignedEmployee() != null) {
            dto.setAssignedEmployeeId(
                    ticket.getAssignedEmployee().getEmployeeId());

            dto.setAssignedEmployeeName(
                    ticket.getAssignedEmployee().getEmployeeName());
        }

        return dto;
    }

    private SupportTicketDto mapToCustomerDto(SupportTicket ticket) {

        SupportTicketDto dto = mapToDto(ticket);

        dto.setResolutionRemarks(null);
        dto.setAssignedEmployeeId(null);
        dto.setAssignedEmployeeName(null);

        return dto;
    }

    // ---------------------------------------------------------
    // CONVERSATION MESSAGES
    // ---------------------------------------------------------

    private TicketConversationMessageDto saveConversationMessage(
            SupportTicket ticket,
            String senderType,
            String senderName,
            String message) {

        TicketConversationMessage item =
                new TicketConversationMessage();

        item.setTicket(ticket);
        item.setSenderType(senderType);
        item.setSenderName(senderName);
        item.setMessage(message);

        TicketConversationMessage saved =
                conversationRepository.save(item);

        return new TicketConversationMessageDto(
                saved.getMessageId(),
                ticket.getTicketId(),
                saved.getSenderType(),
                saved.getSenderName(),
                saved.getMessage(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketConversationMessageDto> getCustomerConversation(
            Long customerId,
            Long ticketId) {

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        if (!ticket.getCustomer().getCustomerId().equals(customerId)) {
            throw new AccountException(
                    "You are not allowed to view this conversation");
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
                        message.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketConversationMessageDto addCustomerReply(
            Long customerId,
            Long ticketId,
            String message) {

        if (message == null || message.isBlank()) {
            throw new AccountException("Reply cannot be empty");
        }

        if (message.trim().length() > 4000) {
            throw new AccountException(
                    "Reply cannot exceed 4000 characters");
        }

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        if (!ticket.getCustomer().getCustomerId().equals(customerId)) {
            throw new AccountException(
                    "You are not allowed to reply to this ticket");
        }

        // Prevent customer replies after the conversation has ended.
        if (ticket.isConversationEnded()) {
            throw new AccountException(
                    "This conversation has ended. Please raise a new support ticket.");
        }

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "This ticket is closed. You cannot reply.");
        }

        return saveConversationMessage(
                ticket,
                "CUSTOMER",
                ticket.getCustomer().getCustomerName(),
                message.trim()
        );
    }


    // ---------------------------------------------------------
    // CUSTOMER TICKETS
    // ---------------------------------------------------------

    @Override
    @Audited(action = "SUPPORT_TICKET_CREATED", details = "Support ticket created for a customer")
    @Transactional
    public SupportTicketDto createTicket(
            Long customerId, SupportTicketDto dto) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new AccountException("Customer not found"));

        if (dto.getSubject() == null || dto.getSubject().isBlank()
                || dto.getDescription() == null
                || dto.getDescription().isBlank()
                || dto.getCategory() == null
                || dto.getCategory().isBlank()) {

            throw new AccountException(
                    "Category, subject and description are required");
        }

        SupportTicket ticket = new SupportTicket();

        ticket.setCustomer(customer);
        ticket.setCategory(dto.getCategory().trim());
        ticket.setSubject(dto.getSubject().trim());
        ticket.setDescription(dto.getDescription().trim());

        if (dto.getCustomerRemarks() != null
                && !dto.getCustomerRemarks().isBlank()) {

            ticket.setCustomerRemarks(
                    dto.getCustomerRemarks().trim());
        }

        String priority = dto.getPriority() == null
                ? "MEDIUM"
                : dto.getPriority().trim().toUpperCase();

        if (!List.of("LOW", "MEDIUM", "HIGH", "URGENT")
                .contains(priority)) {
            throw new AccountException("Invalid ticket priority");
        }

        ticket.setPriority(priority);
        ticket.setStatus("OPEN");

        LocalDateTime createdAt = LocalDateTime.now();

        ticket.setCreatedAt(createdAt);
        ticket.setSlaDeadline(
                calculateSlaDeadline(priority, createdAt));

        SupportTicket savedTicket = ticketRepository.save(ticket);

        // Save the original complaint as the first conversation message.
        saveConversationMessage(
                savedTicket,
                "CUSTOMER",
                customer.getCustomerName(),
                savedTicket.getDescription()
        );

        return mapToCustomerDto(savedTicket);
    }

    @Override
    @Transactional
    public SupportTicketDto createTicketOnBehalf(
            Long customerId, SupportTicketDto dto, Long helpdeskUserId, String channel) {

        // Only regular Helpdesk agents raise customer tickets. The Helpdesk
        // Manager supervises assignment and can reassign tickets afterwards.
        Employee helpdeskEmployee = getRegularHelpdeskEmployee(helpdeskUserId);

        String safeChannel = switch (channel == null ? "" : channel.trim().toUpperCase()) {
            case "BRANCH_VISIT" -> "Branch visit";
            case "EMAIL" -> "Email";
            default -> "Phone call";
        };

        // Reuse the normal customer ticket rules (validation, priority, SLA, first message).
        SupportTicketDto created = createTicket(customerId, dto);

        SupportTicket ticket = ticketRepository.findById(created.getTicketId())
                .orElseThrow(() -> new AccountException("Ticket not found"));

        ticket.setAssignedEmployee(helpdeskEmployee);
        ticketRepository.save(ticket);

        saveConversationMessage(
                ticket,
                "HELPDESK",
                helpdeskEmployee.getEmployeeName(),
                "Ticket raised on behalf of the customer by "
                        + helpdeskEmployee.getEmployeeName()
                        + " (" + safeChannel + ").");

        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getCustomerTickets(Long customerId) {

        return ticketRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToCustomerDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // EMPLOYEE VALIDATION
    // ---------------------------------------------------------

    private Employee getHelpdeskEmployee(Long userId) {

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountException("Employee profile not found"));

        if (employee.getUser() == null
                || employee.getUser().getRole() != Role.HELPDESK) {

            throw new AccountException(
                    "Only Helpdesk users can perform this action");
        }

        return employee;
    }

    private Employee getHelpdeskManager(Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        if (!MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            throw new AccountException(
                    "Only the Helpdesk Manager can perform this action");
        }

        return employee;
    }

    private Employee getRegularHelpdeskEmployee(Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        if (MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            throw new AccountException(
                    "This action is not available to the Helpdesk Manager");
        }

        return employee;
    }

    private void verifyTicketAssignment(
            SupportTicket ticket, Employee employee) {

        if (ticket.getAssignedEmployee() == null
                || !ticket.getAssignedEmployee().getEmployeeId()
                .equals(employee.getEmployeeId())) {

            throw new AccountException("Ticket is not assigned to you");
        }
    }

    // ---------------------------------------------------------
    // INTERNAL INVESTIGATION HISTORY
    // ---------------------------------------------------------

    private void saveTicketRemark(
            SupportTicket ticket,
            Employee employee,
            String message) {

        TicketRemark remark = new TicketRemark();

        remark.setTicket(ticket);
        remark.setEmployee(employee);
        remark.setRemarks(message);

        remarkRepository.save(remark);
    }

    // ---------------------------------------------------------
    // MANAGER: VIEW ALL TICKETS
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getAllTickets(Long userId) {

        getHelpdeskManager(userId);

        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // EMPLOYEE: UPDATE TICKET PROGRESS
    // ---------------------------------------------------------

    @Override
    @Transactional
    public SupportTicketDto updateTicket(
            Long ticketId,
            Long userId,
            String status,
            String remarks) {

        Employee employee = getRegularHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

        if (status == null || status.isBlank()) {
            throw new AccountException("Ticket status is required");
        }

        if (!"IN_PROGRESS".equalsIgnoreCase(status.trim())) {
            throw new AccountException(
                    "Employees can only update tickets to IN_PROGRESS. "
                            + "Submit completed tickets for Manager review.");
        }

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())
                || "PENDING_REVIEW".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "This ticket cannot be updated in its current status");
        }

        ticket.setStatus("IN_PROGRESS");

        if (remarks != null && !remarks.isBlank()) {
            String progressNote = remarks.trim();

            ticket.setResolutionRemarks(progressNote);

            saveTicketRemark(
                    ticket,
                    employee,
                    "Progress update: " + progressNote
            );
        }

        return mapToDto(ticketRepository.save(ticket));
    }

    // ---------------------------------------------------------
    // EMPLOYEE: SEND CUSTOMER-VISIBLE UPDATE
    // ---------------------------------------------------------

    @Override
    @Transactional
    public SupportTicketDto sendCustomerUpdate(
            Long ticketId,
            Long userId,
            String message) {

        Employee employee = getRegularHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

        if (message == null || message.isBlank()) {
            throw new AccountException(
                    "Please enter a customer update");
        }

        if (ticket.isConversationEnded()) {
            throw new AccountException("This conversation has ended. No further replies are allowed.");
        }

        if (message.trim().length() > 4000) {
            throw new AccountException(
                    "Customer update cannot exceed 4000 characters");
        }

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "Cannot update a closed ticket");
        }

        // Save as a customer-visible conversation message.
        // Do not append this to customerRemarks or TicketRemark.
        saveConversationMessage(
                ticket,
                "HELPDESK",
                employee.getEmployeeName(),
                message.trim()
        );

        return mapToDto(ticket);
    }

    // ---------------------------------------------------------
    // MANAGER: ASSIGN OR REASSIGN TICKETS
    // ---------------------------------------------------------

    @Override
    @Transactional
    @Audited(
            action = "HELPDESK_TICKET_ASSIGNED_OR_REASSIGNED",
            details = "Helpdesk Manager assigned or reassigned a support ticket"
    )
    public SupportTicketDto assignTicket(
            Long ticketId,
            Long currentUserId,
            Long employeeId) {

        Employee manager = getHelpdeskManager(currentUserId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        Employee newEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new AccountException("Employee not found"));

        if (newEmployee.getUser() == null
                || newEmployee.getUser().getRole() != Role.HELPDESK
                || MANAGER.equalsIgnoreCase(newEmployee.getDesignation())) {

            throw new AccountException(
                    "Tickets can only be assigned to Helpdesk employees");
        }

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "Closed tickets cannot be assigned");
        }

        Employee previousEmployee = ticket.getAssignedEmployee();

        if (previousEmployee != null
                && previousEmployee.getEmployeeId()
                .equals(newEmployee.getEmployeeId())) {

            return mapToDto(ticket);
        }

        ticket.setAssignedEmployee(newEmployee);

        String assignmentMessage;

        if (previousEmployee == null) {
            assignmentMessage = "Ticket assigned to "
                    + newEmployee.getEmployeeName();
        } else {
            assignmentMessage = "Ticket reassigned from "
                    + previousEmployee.getEmployeeName()
                    + " to "
                    + newEmployee.getEmployeeName();
        }

        saveTicketRemark(ticket, manager, assignmentMessage);

        if ("OPEN".equalsIgnoreCase(ticket.getStatus())
                || "PENDING_REVIEW".equalsIgnoreCase(ticket.getStatus())
                || "RESOLVED".equalsIgnoreCase(ticket.getStatus())) {

            ticket.setStatus("IN_PROGRESS");
        }

        // SLA deadline is not reset during reassignment.
        return mapToDto(ticketRepository.save(ticket));
    }

    // ---------------------------------------------------------
    // EMPLOYEE: SUBMIT FOR MANAGER REVIEW
    // ---------------------------------------------------------

    @Override
    @Transactional
    public SupportTicketDto submitForReview(
            Long ticketId,
            Long userId,
            String remarks) {

        Employee employee = getRegularHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

        if (!"IN_PROGRESS".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "Only IN_PROGRESS tickets can be submitted for review");
        }

        if (remarks == null || remarks.trim().length() < 20) {
            throw new AccountException(
                    "Provide a detailed resolution summary of at least 20 characters");
        }

        String reviewNote = remarks.trim();

        ticket.setResolutionRemarks(reviewNote);

        saveTicketRemark(
                ticket,
                employee,
                "Submitted for manager review: " + reviewNote
        );

        ticket.setStatus("PENDING_REVIEW");

        return mapToDto(ticketRepository.save(ticket));
    }

    // ---------------------------------------------------------
    // MANAGER: REVIEW TICKET
    // ---------------------------------------------------------

    @Override
    @Transactional
    @Audited(
            action = "HELPDESK_TICKET_REVIEWED",
            details = "Helpdesk Manager closed or returned a support ticket"
    )
    public SupportTicketDto reviewTicket(
            Long ticketId,
            Long managerUserId,
            String decision,
            String remarks,
            Long newEmployeeId) {

        Employee manager = getHelpdeskManager(managerUserId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        if (!"PENDING_REVIEW".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "Only tickets pending review can be reviewed");
        }

        if (decision == null || decision.isBlank()) {
            throw new AccountException("Review decision is required");
        }

        String action = decision.trim().toUpperCase();

        if ("CLOSE".equals(action)) {

            if (ticket.getResolutionRemarks() == null
                    || ticket.getResolutionRemarks().trim().length() < 20) {

                throw new AccountException(
                        "Cannot close ticket without a detailed employee resolution summary");
            }

            ticket.setStatus("CLOSED");

            if (remarks != null && !remarks.isBlank()) {
                saveTicketRemark(
                        ticket,
                        manager,
                        "Manager closed ticket: " + remarks.trim()
                );
            }

        } else if ("RETURN".equals(action)) {

            if (remarks == null || remarks.isBlank()) {
                throw new AccountException(
                        "Please provide a reason before returning the ticket");
            }

            if (newEmployeeId == null) {
                throw new AccountException(
                        "Please select an employee to assign the returned ticket");
            }

            Employee newEmployee = employeeRepository.findById(newEmployeeId)
                    .orElseThrow(() ->
                            new AccountException("Employee not found"));

            if (newEmployee.getUser() == null
                    || newEmployee.getUser().getRole() != Role.HELPDESK
                    || MANAGER.equalsIgnoreCase(newEmployee.getDesignation())) {

                throw new AccountException(
                        "Select a valid Helpdesk employee");
            }

            Employee previousEmployee = ticket.getAssignedEmployee();

            if (previousEmployee == null
                    || !previousEmployee.getEmployeeId()
                    .equals(newEmployee.getEmployeeId())) {

                ticket.setAssignedEmployee(newEmployee);

                String assignmentMessage = previousEmployee == null
                        ? "Returned ticket assigned to "
                        + newEmployee.getEmployeeName()
                        : "Returned ticket reassigned from "
                        + previousEmployee.getEmployeeName()
                        + " to "
                        + newEmployee.getEmployeeName();

                saveTicketRemark(ticket, manager, assignmentMessage);
            }

            ticket.setStatus("IN_PROGRESS");

            saveTicketRemark(
                    ticket,
                    manager,
                    "Manager returned ticket: " + remarks.trim()
            );

        } else {
            throw new AccountException(
                    "Invalid decision. Use CLOSE or RETURN");
        }

        return mapToDto(ticketRepository.save(ticket));
    }

    // ---------------------------------------------------------
    // EMPLOYEE: VIEW ASSIGNED TICKETS
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getMyAssignedTickets(Long userId) {

        Employee employee = getRegularHelpdeskEmployee(userId);

        return ticketRepository
                .findByAssignedEmployeeEmployeeId(employee.getEmployeeId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // INTERNAL INVESTIGATION REMARKS
    // ---------------------------------------------------------

    @Override
    @Transactional
    public TicketRemarkDto addRemark(
            Long ticketId,
            Long userId,
            String remarks) {

        if (remarks == null || remarks.isBlank()) {
            throw new AccountException("Remarks cannot be empty");
        }

        Employee employee = getHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        if (!MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            verifyTicketAssignment(ticket, employee);
        }

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "Remarks cannot be added to a closed ticket");
        }

        TicketRemark remark = new TicketRemark();

        remark.setTicket(ticket);
        remark.setEmployee(employee);
        remark.setRemarks(remarks.trim());

        TicketRemark saved = remarkRepository.save(remark);

        return new TicketRemarkDto(
                saved.getRemarkId(),
                ticketId,
                employee.getEmployeeName(),
                saved.getRemarks(),
                saved.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketRemarkDto> getTicketRemarks(
            Long ticketId,
            Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        if (!MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            verifyTicketAssignment(ticket, employee);
        }

        return remarkRepository
                .findByTicketTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(remark -> new TicketRemarkDto(
                        remark.getRemarkId(),
                        ticketId,
                        remark.getEmployee().getEmployeeName(),
                        remark.getRemarks(),
                        remark.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // STATISTICS
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTicketStatistics(Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        List<SupportTicket> tickets;

        if (MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        } else {
            tickets = ticketRepository
                    .findByAssignedEmployeeEmployeeId(employee.getEmployeeId());
        }

        Map<String, Long> statistics = new HashMap<>();

        statistics.put("total", (long) tickets.size());
        statistics.put("open", countStatus(tickets, "OPEN"));
        statistics.put("inProgress", countStatus(tickets, "IN_PROGRESS"));
        statistics.put("pendingReview", countStatus(tickets, "PENDING_REVIEW"));
        statistics.put("resolved", countStatus(tickets, "RESOLVED"));
        statistics.put("closed", countStatus(tickets, "CLOSED"));
        statistics.put("overdue",
                tickets.stream().filter(this::isTicketOverdue).count());
        statistics.put(
                "escalated",
                tickets.stream()
                        .filter(SupportTicket::isEscalated)
                        .count()
        );

        return statistics;
    }

    private long countStatus(
            List<SupportTicket> tickets,
            String status) {

        return tickets.stream()
                .filter(ticket ->
                        status.equalsIgnoreCase(ticket.getStatus()))
                .count();
    }

    // ---------------------------------------------------------
    // FILTER TICKETS
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> filterTickets(
            Long userId,
            String status,
            String priority) {

        Employee employee = getHelpdeskEmployee(userId);

        List<SupportTicket> tickets;

        if (MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        } else {
            tickets = ticketRepository
                    .findByAssignedEmployeeEmployeeId(employee.getEmployeeId());
        }

        return tickets.stream()
                .filter(ticket ->
                        status == null || status.isBlank()
                                || status.trim().equalsIgnoreCase(ticket.getStatus()))
                .filter(ticket ->
                        priority == null || priority.isBlank()
                                || priority.trim().equalsIgnoreCase(ticket.getPriority()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------
    // MANAGER: GET ASSIGNABLE EMPLOYEES
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<HelpdeskEmployeeDto> getHelpdeskEmployees(Long userId) {

        getHelpdeskManager(userId);

        return employeeRepository.findAll()
                .stream()
                .filter(employee ->
                        employee.getUser() != null
                                && employee.getUser().getRole() == Role.HELPDESK
                                && !MANAGER.equalsIgnoreCase(
                                employee.getDesignation()))
                .map(employee -> new HelpdeskEmployeeDto(
                        employee.getEmployeeId(),
                        employee.getEmployeeName()
                ))
                .collect(Collectors.toList());
    }

    // Employee: End customer-facing conversation without closing the ticket
    @Override
    @Transactional
    public SupportTicketDto endConversation(Long ticketId, Long userId) {

        Employee employee = getRegularHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

        if (ticket.isConversationEnded()) {
            throw new AccountException("This conversation has already ended");
        }

        ticket.setConversationEnded(true);
        ticket.setConversationEndedAt(LocalDateTime.now());
        ticket.setConversationEndedBy(employee.getEmployeeName());

        String closingMessage =
                "This conversation has been ended by " +
                        employee.getEmployeeName() +
                        ". Thank you for contacting WebDisplay Support. " +
                        "If you need further assistance, please raise a new support ticket.";

        saveConversationMessage(
                ticket,
                "HELPDESK",
                employee.getEmployeeName(),
                closingMessage
        );

        return mapToDto(ticketRepository.save(ticket));
    }

}