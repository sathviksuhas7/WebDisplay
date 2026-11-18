package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.dto.TicketConversationMessageDto;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.service.impl.HelpdeskTicketConversationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/tickets")
public class HelpdeskTicketConversationController {

    private final HelpdeskTicketConversationService conversationService;

    public HelpdeskTicketConversationController(
            HelpdeskTicketConversationService conversationService) {

        this.conversationService = conversationService;
    }

    @GetMapping("/{ticketId}/conversation")
    public ResponseEntity<List<TicketConversationMessageDto>> getConversation(
            @PathVariable Long ticketId,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            throw new RuntimeException(
                    "Session expired. Please log in again.");
        }

        return ResponseEntity.ok(
                conversationService.getConversation(
                        user.getId(), ticketId));
    }
}