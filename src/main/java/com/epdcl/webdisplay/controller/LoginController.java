
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.service.impl.UserService;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    // SHOW LOGIN PAGE
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {

        session.setAttribute("csrfToken", UUID.randomUUID().toString());

        return "login";
    }

    // HANDLE LOGIN
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String csrfToken,
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        // VALIDATE CSRF TOKEN
        String sessionToken = (String) session.getAttribute("csrfToken");

        if (sessionToken == null || !sessionToken.equals(csrfToken)) {

            session.setAttribute("csrfToken", UUID.randomUUID().toString());

            model.addAttribute("error",
                    "Invalid session. Please try logging in again.");

            return "login";
        }

        // AUTHENTICATE USER
        User user = userService.authenticateUser(username, password);

        if (user != null) {

            // PREVENT SESSION FIXATION
            request.changeSessionId();

            // GENERATE NEW CSRF TOKEN AFTER LOGIN
            session.setAttribute("csrfToken", UUID.randomUUID().toString());

            // STORE LOGGED-IN USER
            session.setAttribute("loggedInUser", user);
            session.setAttribute("role", user.getRole().name());

            // REDIRECT BASED ON ROLE
            if (user.getRole() == Role.ADMIN) {
                return "redirect:/admin/dashboard";
            }

            if (user.getRole() == Role.EMPLOYEE) {
                return "redirect:/employee/dashboard";
            }

            if (user.getRole() == Role.CUSTOMER) {
                return "redirect:/customer/dashboard";
            }

            if (user.getRole() == Role.HELPDESK) {
                return "redirect:/helpdesk/dashboard";
            }
        }

        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    // HANDLE LOGOUT
    @PostMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login?logout=true";
    }
}
