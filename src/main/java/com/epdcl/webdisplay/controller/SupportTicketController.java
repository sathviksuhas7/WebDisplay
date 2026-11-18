
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;
import com.epdcl.webdisplay.dto.HelpdeskEmployeeDto;
import com.epdcl.webdisplay.dto.TicketConversationMessageDto;

import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.Employee;
import com.epdcl.webdisplay.entity.User;

import com.epdcl.webdisplay.repo.EmployeeRepository;
import com.epdcl.webdisplay.service.impl.CustomerService;
import com.epdcl.webdisplay.service.impl.SupportTicketService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SupportTicketController {

    private final EmployeeRepository employeeRepository;
    private final SupportTicketService ticketService;
    private final CustomerService customerService;

    public SupportTicketController(
            SupportTicketService ticketService,
            CustomerService customerService,
            EmployeeRepository employeeRepository) {

        this.ticketService = ticketService;
        this.customerService = customerService;
        this.employeeRepository = employeeRepository;
    }

    private User getLoggedInUser(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            throw new RuntimeException("Session expired. Please log in again.");
        }

        return user;
    }

    // CUSTOMER: Create ticket
    @PostMapping("/customer/tickets")
    public ResponseEntity<SupportTicketDto> createTicket(
            @RequestBody SupportTicketDto dto,
            HttpSession session) {

        User user = getLoggedInUser(session);
        Customer customer = customerService.getCustomerByUserId(user.getId());

        return ResponseEntity.ok(
                ticketService.createTicket(customer.getCustomerId(), dto)
        );
    }

    // CUSTOMER: View own tickets
    @GetMapping("/customer/tickets")
    public ResponseEntity<List<SupportTicketDto>> getMyTickets(
            HttpSession session) {

        User user = getLoggedInUser(session);
        Customer customer = customerService.getCustomerByUserId(user.getId());

        return ResponseEntity.ok(
                ticketService.getCustomerTickets(customer.getCustomerId())
        );
    }

    // CUSTOMER: View own ticket conversation
    @GetMapping("/customer/tickets/{ticketId}/conversation")
    public ResponseEntity<List<TicketConversationMessageDto>>
    getCustomerConversation(
            @PathVariable Long ticketId,
            HttpSession session) {

        User user = getLoggedInUser(session);
        Customer customer = customerService.getCustomerByUserId(user.getId());

        return ResponseEntity.ok(
                ticketService.getCustomerConversation(
                        customer.getCustomerId(),
                        ticketId
                )
        );
    }

    // CUSTOMER: Reply to own ticket
    @PostMapping("/customer/tickets/{ticketId}/conversation")
    public ResponseEntity<TicketConversationMessageDto> addCustomerReply(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);
        Customer customer = customerService.getCustomerByUserId(user.getId());

        return ResponseEntity.ok(
                ticketService.addCustomerReply(
                        customer.getCustomerId(),
                        ticketId,
                        request.get("message")
                )
        );
    }

    // MANAGER: View all tickets
    @GetMapping("/helpdesk/tickets")
    public ResponseEntity<List<SupportTicketDto>> getAllTickets(
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.getAllTickets(user.getId())
        );
    }

    // EMPLOYEE: Update progress on assigned ticket
    @PutMapping("/helpdesk/tickets/{ticketId}")
    public ResponseEntity<SupportTicketDto> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.updateTicket(
                        ticketId,
                        user.getId(),
                        request.get("status"),
                        request.get("remarks")
                )
        );
    }

    // MANAGER: Assign or reassign ticket
    @PutMapping("/helpdesk/tickets/{ticketId}/assign/{employeeId}")
    public ResponseEntity<SupportTicketDto> assignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long employeeId,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.assignTicket(
                        ticketId,
                        user.getId(),
                        employeeId
                )
        );
    }

    // EMPLOYEE: Send customer-visible update
    @PutMapping("/helpdesk/tickets/{ticketId}/customer-update")
    public ResponseEntity<SupportTicketDto> sendCustomerUpdate(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.sendCustomerUpdate(
                        ticketId,
                        user.getId(),
                        request.get("message")
                )
        );
    }

    // EMPLOYEE: Submit work for manager review
    @PutMapping("/helpdesk/tickets/{ticketId}/submit-review")
    public ResponseEntity<SupportTicketDto> submitForReview(
            @PathVariable Long ticketId,
            @RequestBody(required = false) Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);

        String remarks = request == null ? null : request.get("remarks");

        return ResponseEntity.ok(
                ticketService.submitForReview(
                        ticketId,
                        user.getId(),
                        remarks
                )
        );
    }

    // MANAGER: Close or return ticket
    @PutMapping("/helpdesk/tickets/{ticketId}/review")
    public ResponseEntity<SupportTicketDto> reviewTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.reviewTicket(
                        ticketId,
                        user.getId(),
                        request.get("decision"),
                        request.get("remarks"),
                        request.get("employeeId") == null
                                || request.get("employeeId").isBlank()
                                ? null
                                : Long.valueOf(request.get("employeeId"))
                )
        );
    }

    // EMPLOYEE: View assigned tickets
    @GetMapping("/helpdesk/my-tickets")
    public ResponseEntity<List<SupportTicketDto>> getMyAssignedTickets(
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.getMyAssignedTickets(user.getId())
        );
    }

    // EMPLOYEE: Add internal investigation remark
    @PostMapping("/helpdesk/tickets/{ticketId}/remarks")
    public ResponseEntity<TicketRemarkDto> addRemark(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.addRemark(
                        ticketId,
                        user.getId(),
                        request.get("remarks")
                )
        );
    }

    // EMPLOYEE OR MANAGER: View internal investigation history
    @GetMapping("/helpdesk/tickets/{ticketId}/remarks")
    public ResponseEntity<List<TicketRemarkDto>> getTicketRemarks(
            @PathVariable Long ticketId,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.getTicketRemarks(
                        ticketId,
                        user.getId()
                )
        );
    }

    // MANAGER: View statistics
    @GetMapping("/helpdesk/tickets/statistics")
    public ResponseEntity<Map<String, Long>> getTicketStatistics(
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.getTicketStatistics(user.getId())
        );
    }

    // MANAGER OR EMPLOYEE: Filter tickets
    @GetMapping("/helpdesk/tickets/filter")
    public ResponseEntity<List<SupportTicketDto>> filterTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.filterTickets(
                        user.getId(),
                        status,
                        priority
                )
        );
    }

    // MANAGER: Get assignable Helpdesk employees
    @GetMapping("/helpdesk/employees")
    public ResponseEntity<List<HelpdeskEmployeeDto>> getHelpdeskEmployees(
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.getHelpdeskEmployees(user.getId())
        );
    }

    // HELPDESK: Get logged-in employee profile
    @GetMapping("/helpdesk/me")
    public ResponseEntity<Map<String, String>> getLoggedInHelpdesk(
            HttpSession session) {

        User user = getLoggedInUser(session);

        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Employee profile not found"));

        return ResponseEntity.ok(
                Map.of(
                        "employeeName", employee.getEmployeeName(),
                        "designation", employee.getDesignation() == null
                                ? "HELPDESK_EMPLOYEE"
                                : employee.getDesignation()
                )
        );
    }

    // EMPLOYEE: End customer-facing conversation
    @PutMapping("/helpdesk/tickets/{ticketId}/conversation/end")
    public ResponseEntity<SupportTicketDto> endConversation(
            @PathVariable Long ticketId,
            HttpSession session) {

        User user = getLoggedInUser(session);

        return ResponseEntity.ok(
                ticketService.endConversation(ticketId, user.getId())
        );
    }
}
