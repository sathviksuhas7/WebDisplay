package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;
import com.epdcl.webdisplay.dto.HelpdeskEmployeeDto;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.SupportTicket;
import com.epdcl.webdisplay.entity.TicketRemark;
import com.epdcl.webdisplay.exception.AccountException;
import com.epdcl.webdisplay.repo.CustomerRepository;
import com.epdcl.webdisplay.repo.EmployeeRepository;
import com.epdcl.webdisplay.repo.SupportTicketRepository;
import com.epdcl.webdisplay.repo.TicketRemarkRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    private static final String MANAGER = "HELPDESK_MANAGER";
    private static final String EMPLOYEE = "HELPDESK_EMPLOYEE";

    private final TicketRemarkRepository remarkRepository;
    private final SupportTicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public SupportTicketServiceImpl(
            SupportTicketRepository ticketRepository,
            CustomerRepository customerRepository,
            EmployeeRepository employeeRepository,
            TicketRemarkRepository remarkRepository) {

        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.remarkRepository = remarkRepository;
    }

    // Convert ticket entity to DTO
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

        if (ticket.getAssignedEmployee() != null) {
            dto.setAssignedEmployeeId(
                    ticket.getAssignedEmployee().getEmployeeId());

            dto.setAssignedEmployeeName(
                    ticket.getAssignedEmployee().getEmployeeName());
        }

        // Display latest remark with its author's name
        remarkRepository
                .findFirstByTicketTicketIdOrderByCreatedAtDescRemarkIdDesc(
                        ticket.getTicketId())
                .ifPresentOrElse(
                        remark -> dto.setResolutionRemarks(
                                remark.getEmployee().getEmployeeName()
                                        + ": " + remark.getRemarks()),
                        () -> dto.setResolutionRemarks(
                                ticket.getResolutionRemarks())
                );

        return dto;
    }

    // Customer-facing DTO: hide internal remarks and assignment details
    private SupportTicketDto mapToCustomerDto(SupportTicket ticket) {

        SupportTicketDto dto = mapToDto(ticket);

        dto.setResolutionRemarks(null);
        dto.setAssignedEmployeeId(null);
        dto.setAssignedEmployeeName(null);
        dto.setCustomerRemarks(ticket.getCustomerRemarks());

        return dto;
    }

    // Create customer ticket
    @Override
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

        String priority = dto.getPriority() == null
                ? "MEDIUM"
                : dto.getPriority().trim().toUpperCase();

        if (!List.of("LOW", "MEDIUM", "HIGH", "URGENT")
                .contains(priority)) {
            throw new AccountException("Invalid ticket priority");
        }

        ticket.setPriority(priority);
        ticket.setStatus("OPEN");

        return mapToCustomerDto(ticketRepository.save(ticket));
    }

    // Customer views own tickets
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getCustomerTickets(Long customerId) {

        return ticketRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToCustomerDto)
                .collect(Collectors.toList());
    }

    // Get Helpdesk employee by user ID
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

    // Verify Manager designation
    private Employee getHelpdeskManager(Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        if (!MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            throw new AccountException(
                    "Only the Helpdesk Manager can perform this action");
        }

        return employee;
    }

    // Verify regular Helpdesk employee
    private Employee getRegularHelpdeskEmployee(Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        if (MANAGER.equalsIgnoreCase(employee.getDesignation())) {
            throw new AccountException(
                    "This action is not available to the Helpdesk Manager");
        }

        return employee;
    }

    // Verify ticket assignment
    private void verifyTicketAssignment(
            SupportTicket ticket, Employee employee) {

        if (ticket.getAssignedEmployee() == null
                || !ticket.getAssignedEmployee().getEmployeeId()
                .equals(employee.getEmployeeId())) {

            throw new AccountException("Ticket is not assigned to you");
        }
    }

    // Save employee and manager messages in shared ticket history
    private void saveTicketRemark(
            SupportTicket ticket, Employee employee, String message) {

        TicketRemark remark = new TicketRemark();
        remark.setTicket(ticket);
        remark.setEmployee(employee);
        remark.setRemarks(message);

        remarkRepository.save(remark);
    }

    // Manager: View all tickets
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getAllTickets(Long userId) {

        getHelpdeskManager(userId);

        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Employee: Update assigned ticket progress
    @Override
    @Transactional
    public SupportTicketDto updateTicket(
            Long ticketId, Long userId,
            String status, String remarks) {

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
                    ticket, employee, "Progress update: " + progressNote);
        }

        return mapToDto(ticketRepository.save(ticket));
    }

    // Employee: Send an intentional customer-facing update
    @Override
    @Transactional
    public SupportTicketDto sendCustomerUpdate(
            Long ticketId, Long userId, String message) {

        Employee employee = getRegularHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

        if (message == null || message.isBlank()) {
            throw new AccountException(
                    "Customer update message cannot be empty");
        }

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "Cannot send an update for a closed ticket");
        }

        ticket.setCustomerRemarks(message.trim());

        return mapToDto(ticketRepository.save(ticket));
    }

    // Manager: Assign or reassign tickets
    @Override
    @Transactional
    public SupportTicketDto assignTicket(
            Long ticketId, Long currentUserId, Long employeeId) {

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

        // Prevent unnecessary reassignment to the same employee
        if (previousEmployee != null
                && previousEmployee.getEmployeeId()
                .equals(newEmployee.getEmployeeId())) {

            return mapToDto(ticket);
        }

        // Change current assignment
        ticket.setAssignedEmployee(newEmployee);

        // Record assignment/reassignment in permanent history
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

        return mapToDto(ticketRepository.save(ticket));
    }

    // Employee: Submit completed work for Manager review
    @Override
    @Transactional
    public SupportTicketDto submitForReview(
            Long ticketId, Long userId, String remarks) {

        Employee employee = getRegularHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

        if (!"IN_PROGRESS".equalsIgnoreCase(ticket.getStatus())) {
            throw new AccountException(
                    "Only IN_PROGRESS tickets can be submitted for review");
        }

        if (remarks != null && !remarks.isBlank()) {
            String reviewNote = remarks.trim();
            ticket.setResolutionRemarks(reviewNote);

            saveTicketRemark(
                    ticket, employee,
                    "Submitted for manager review: " + reviewNote);
        }

        ticket.setStatus("PENDING_REVIEW");

        return mapToDto(ticketRepository.save(ticket));
    }

    // Manager: Close ticket or return it to employee
    @Override
    @Transactional
    public SupportTicketDto reviewTicket(
            Long ticketId, Long managerUserId,
            String decision, String remarks) {

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

            ticket.setStatus("CLOSED");

            if (remarks != null && !remarks.isBlank()) {
                String managerNote = remarks.trim();
                ticket.setResolutionRemarks(managerNote);

                saveTicketRemark(
                        ticket, manager,
                        "Manager closed ticket: " + managerNote);
            }

        } else if ("RETURN".equals(action)) {

            if (remarks == null || remarks.isBlank()) {
                throw new AccountException(
                        "Please provide a reason before returning the ticket");
            }

            ticket.setStatus("IN_PROGRESS");

            String managerNote = remarks.trim();
            ticket.setResolutionRemarks(managerNote);

            saveTicketRemark(
                    ticket, manager,
                    "Manager returned ticket: " + managerNote);

        } else {

            throw new AccountException(
                    "Invalid decision. Use CLOSE or RETURN");
        }

        return mapToDto(ticketRepository.save(ticket));
    }

    // Employee: View assigned tickets
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

    // Add investigation remark
    @Override
    @Transactional
    public TicketRemarkDto addRemark(
            Long ticketId, Long userId, String remarks) {

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

    // View complete investigation history
    @Override
    @Transactional(readOnly = true)
    public List<TicketRemarkDto> getTicketRemarks(
            Long ticketId, Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        // Manager can view all ticket histories.
        // Employee can view the history of their currently assigned ticket.
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

    // Statistics limited to the logged-in user's permitted tickets
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

        return statistics;
    }

    private long countStatus(List<SupportTicket> tickets, String status) {

        return tickets.stream()
                .filter(ticket -> status.equalsIgnoreCase(ticket.getStatus()))
                .count();
    }

    // Filter tickets within the user's permitted scope
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> filterTickets(
            Long userId, String status, String priority) {

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

    // Manager: Get assignable Helpdesk employees
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


}