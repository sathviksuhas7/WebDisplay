
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.SupportTicketDto;
import com.epdcl.webdisplay.dto.TicketRemarkDto;
import com.epdcl.webdisplay.entity.Customer;
import com.epdcl.webdisplay.entity.User;
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

    private final SupportTicketService ticketService;
    private final CustomerService customerService;

    public SupportTicketController(
            SupportTicketService ticketService,
            CustomerService customerService) {

        this.ticketService = ticketService;
        this.customerService = customerService;
    }

    // Customer raises a ticket
    @PostMapping("/customer/tickets")
    public ResponseEntity<SupportTicketDto> createTicket(
            @RequestBody SupportTicketDto dto,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        return ResponseEntity.ok(
                ticketService.createTicket(
                        customer.getCustomerId(), dto));
    }

    // Customer views their own tickets
    @GetMapping("/customer/tickets")
    public ResponseEntity<List<SupportTicketDto>> getMyTickets(
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        Customer customer =
                customerService.getCustomerByUserId(user.getId());

        return ResponseEntity.ok(
                ticketService.getCustomerTickets(
                        customer.getCustomerId()));
    }

    // Helpdesk views all tickets
    @GetMapping("/helpdesk/tickets")
    public ResponseEntity<List<SupportTicketDto>> getAllTickets() {

        return ResponseEntity.ok(
                ticketService.getAllTickets());
    }

    // Helpdesk updates an assigned ticket
    @PutMapping("/helpdesk/tickets/{ticketId}")
    public ResponseEntity<SupportTicketDto> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        return ResponseEntity.ok(
                ticketService.updateTicket(
                        ticketId,
                        user.getId(),
                        request.get("status"),
                        request.get("remarks")
                )
        );
    }

    // Helpdesk assigns ticket to another Helpdesk employee
    @PutMapping("/helpdesk/tickets/{ticketId}/assign/{employeeId}")
    public ResponseEntity<SupportTicketDto> assignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                ticketService.assignTicket(ticketId, employeeId));
    }

    // Helpdesk views tickets assigned to them
    @GetMapping("/helpdesk/my-tickets")
    public ResponseEntity<List<SupportTicketDto>> getMyAssignedTickets(
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        return ResponseEntity.ok(
                ticketService.getMyAssignedTickets(user.getId()));
    }

    // Helpdesk adds an investigation remark
    @PostMapping("/helpdesk/tickets/{ticketId}/remarks")
    public ResponseEntity<TicketRemarkDto> addRemark(
            @PathVariable Long ticketId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        return ResponseEntity.ok(
                ticketService.addRemark(
                        ticketId,
                        user.getId(),
                        request.get("remarks")
                )
        );
    }

    // Helpdesk views investigation history
    @GetMapping("/helpdesk/tickets/{ticketId}/remarks")
    public ResponseEntity<List<TicketRemarkDto>> getTicketRemarks(
            @PathVariable Long ticketId,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        return ResponseEntity.ok(
                ticketService.getTicketRemarks(
                        ticketId,
                        user.getId()
                )
        );
    }
}
