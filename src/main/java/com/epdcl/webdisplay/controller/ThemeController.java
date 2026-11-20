package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.service.ThemeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping("/theme")
    public ResponseEntity<Map<String, String>> getTheme() {
        return ResponseEntity.ok(Map.of("theme", themeService.getTheme()));
    }

    @PostMapping("/admin/theme")
    public ResponseEntity<Map<String, String>> updateTheme(
            @RequestBody Map<String, String> body, HttpSession session) {
        Object value = session.getAttribute("loggedInUser");
        if (!(value instanceof User user) || user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only an Admin can change the site theme.");
        }
        String saved = themeService.saveTheme(body.get("theme"));
        return ResponseEntity.ok(Map.of(
                "theme", saved,
                "message", "Site theme updated successfully."));
    }
}
