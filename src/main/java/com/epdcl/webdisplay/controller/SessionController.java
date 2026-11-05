package com.epdcl.webdisplay.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SessionController {

    @PostMapping("/api/session/heartbeat")
    public ResponseEntity<Map<String, String>> checkSession(
            HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "SESSION_EXPIRED"));
        }

        return ResponseEntity.ok(
                Map.of("message", "SESSION_ACTIVE"));
    }
}