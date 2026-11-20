
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.repo.EmployeeRepository;
import com.epdcl.webdisplay.service.impl.CustomerService;
import com.epdcl.webdisplay.service.impl.AccountService;
import com.epdcl.webdisplay.service.impl.SupportTicketService;
import com.epdcl.webdisplay.dto.SupportTicketDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.epdcl.webdisplay.audit.AuditService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/helpdesk")
public class HelpdeskController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final EmployeeRepository employeeRepository;
    private final SupportTicketService ticketService;
    private final AuditService auditService;

    public HelpdeskController(
            CustomerService customerService,
            AccountService accountService,
            EmployeeRepository employeeRepository,
            SupportTicketService ticketService,
            AuditService auditService) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.employeeRepository = employeeRepository;
        this.ticketService = ticketService;
        this.auditService = auditService;
    }

    // Allow access only to logged-in Helpdesk employees and managers
    private void checkHelpdeskAccess(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in again."
            );
        }

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Helpdesk access denied."
                ));

        if (employee.getUser().getRole() != Role.HELPDESK) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only Helpdesk employees can access this information."
            );
        }
    }


    /** Create a ticket for a customer who contacted the bank by phone or in person. */
    @PostMapping("/customers/{customerId}/tickets")
    public ResponseEntity<?> createTicketOnBehalf(
            @PathVariable Long customerId,
            @RequestBody SupportTicketDto dto,
            @RequestParam(defaultValue = "PHONE_CALL") String channel,
            HttpSession session,
            HttpServletRequest request) {
        checkHelpdeskAccess(session);
        User user = (User) session.getAttribute("loggedInUser");
        SupportTicketDto created =
                ticketService.createTicketOnBehalf(customerId, dto, user.getId(), channel);
        auditService.recordActivity(
                user,
                "HELPDESK_TICKET_RAISED_FOR_CUSTOMER",
                "Ticket ID " + created.getTicketId() + " raised for customer ID " + customerId
                        + " via " + channel,
                request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/customers/search")
    public ResponseEntity<?> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            HttpSession session) {

        checkHelpdeskAccess(session);

        return ResponseEntity.ok(
                customerService.searchCustomers(name, phone)
        );
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<?> getCustomer(
            @PathVariable Long customerId,
            HttpSession session) {

        checkHelpdeskAccess(session);

        return ResponseEntity.ok(
                customerService.getHelpdeskCustomerById(customerId)
        );
    }

    @GetMapping("/customers/{customerId}/accounts")
    public ResponseEntity<?> getCustomerAccounts(
            @PathVariable Long customerId,
            HttpSession session) {

        checkHelpdeskAccess(session);

        return ResponseEntity.ok(
                accountService.getHelpdeskCustomerAccounts(customerId)
        );
    }

    @GetMapping("/customers/{customerId}/transactions")
    public ResponseEntity<?> getCustomerTransactions(
            @PathVariable Long customerId,
            HttpSession session) {

        checkHelpdeskAccess(session);

        return ResponseEntity.ok(
                accountService.getHelpdeskCustomerTransactions(customerId)
        );
    }
}
