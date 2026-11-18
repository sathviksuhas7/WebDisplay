
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.audit.AuditService;
import com.epdcl.webdisplay.entity.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SessionController {

    private final AuditService auditService;

    public SessionController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping("/api/session/heartbeat")
    public ResponseEntity<Map<String, String>> checkSession(
            HttpSession session) {

        Object loggedInUser = session.getAttribute("loggedInUser");

        if (!(loggedInUser instanceof User user)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "SESSION_EXPIRED"));
        }

        auditService.touchCurrentSession(user, session);

        return ResponseEntity.ok(
                Map.of("message", "SESSION_ACTIVE"));
    }
}
