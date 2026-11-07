package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

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

    // Convert Entity to DTO
    private SupportTicketDto mapToDto(SupportTicket ticket) {

        SupportTicketDto dto = new SupportTicketDto();

        dto.setTicketId(ticket.getTicketId());
        dto.setCustomerId(ticket.getCustomer().getCustomerId());
        dto.setCategory(ticket.getCategory());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setStatus(ticket.getStatus());
        dto.setResolutionRemarks(ticket.getResolutionRemarks());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        return dto;
    }

    // Customer Creates Ticket
    @Override
    @Transactional
    public SupportTicketDto createTicket(Long customerId, SupportTicketDto dto) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AccountException("Customer not found"));

        if (dto.getSubject() == null || dto.getSubject().isBlank()
                || dto.getDescription() == null || dto.getDescription().isBlank()
                || dto.getCategory() == null || dto.getCategory().isBlank()) {

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

        if (!List.of("LOW", "MEDIUM", "HIGH", "URGENT").contains(priority)) {
            throw new AccountException("Invalid ticket priority");
        }

        ticket.setPriority(priority);
        ticket.setStatus("OPEN");

        return mapToDto(ticketRepository.save(ticket));
    }

    // Customer Views Own Tickets
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getCustomerTickets(Long customerId) {

        return ticketRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helpdesk Views All Tickets
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getAllTickets() {

        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Find the logged-in Helpdesk employee
    private Employee getHelpdeskEmployee(Long userId) {

        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountException("Employee profile not found"));

        if (employee.getUser() == null
                || employee.getUser().getRole() != Role.HELPDESK) {

            throw new AccountException(
                    "Only helpdesk employees can perform this action");
        }

        return employee;
    }

    // Check whether the ticket is assigned to this employee
    private void verifyTicketAssignment(
            SupportTicket ticket, Employee employee) {

        if (ticket.getAssignedEmployee() == null
                || !ticket.getAssignedEmployee().getEmployeeId()
                .equals(employee.getEmployeeId())) {

            throw new AccountException("Ticket is not assigned to you");
        }
    }

    // Helpdesk Updates an Assigned Ticket
    @Override
    @Transactional
    public SupportTicketDto updateTicket(
            Long ticketId, Long userId, String status, String remarks) {

        Employee employee = getHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

        if (status == null || status.isBlank()) {
            throw new AccountException("Ticket status is required");
        }

        String newStatus = status.trim().toUpperCase();

        if (!List.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED")
                .contains(newStatus)) {

            throw new AccountException("Invalid ticket status");
        }

        ticket.setStatus(newStatus);
        ticket.setResolutionRemarks(remarks);

        return mapToDto(ticketRepository.save(ticket));
    }

    // Helpdesk Assigns Ticket to Another Helpdesk Employee
    @Override
    @Transactional
    public SupportTicketDto assignTicket(Long ticketId, Long employeeId) {

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new AccountException("Employee not found"));

        if (employee.getUser() == null
                || employee.getUser().getRole() != Role.HELPDESK) {

            throw new AccountException(
                    "Tickets can only be assigned to helpdesk employees");
        }

        ticket.setAssignedEmployee(employee);

        return mapToDto(ticketRepository.save(ticket));
    }

    // Get Tickets Assigned to Logged-in Helpdesk Employee
    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDto> getMyAssignedTickets(Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        return ticketRepository
                .findByAssignedEmployeeEmployeeId(employee.getEmployeeId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Add Investigation Remark
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

        verifyTicketAssignment(ticket, employee);

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

    // View Investigation History
    @Override
    @Transactional(readOnly = true)
    public List<TicketRemarkDto> getTicketRemarks(
            Long ticketId, Long userId) {

        Employee employee = getHelpdeskEmployee(userId);

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new AccountException("Ticket not found"));

        verifyTicketAssignment(ticket, employee);

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
}