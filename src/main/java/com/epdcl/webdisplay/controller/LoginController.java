
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.service.impl.UserService;
import com.epdcl.webdisplay.audit.AuditService;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UserService userService;
    private final AuditService auditService;

    public LoginController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        session.setAttribute("csrfToken", UUID.randomUUID().toString());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String csrfToken,
            HttpServletRequest request,
            HttpSession session,
            Model model) {

        String sessionToken = (String) session.getAttribute("csrfToken");

        if (sessionToken == null || !sessionToken.equals(csrfToken)) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
            model.addAttribute("error",
                    "Invalid session. Please try logging in again.");
            return "login";
        }

        User user = userService.authenticateUser(username, password);

        if (user != null) {

            User previousUser = (User) session.getAttribute("loggedInUser");

            if (previousUser != null) {
                auditService.recordLogout(previousUser, session, request);
            }

            request.changeSessionId();

            session.setAttribute("csrfToken", UUID.randomUUID().toString());
            session.setAttribute("loggedInUser", user);
            session.setAttribute("role", user.getRole().name());

            auditService.registerLogin(user, session, request);

            if (user.getRole() == Role.CUSTOMER
                    && user.isMustChangePassword()) {
                return "redirect:/change-password";
            }

            if (user.getRole() == Role.ADMIN)
                return "redirect:/admin/dashboard";

            if (user.getRole() == Role.EMPLOYEE)
                return "redirect:/employee/dashboard";

            if (user.getRole() == Role.CUSTOMER)
                return "redirect:/customer/dashboard";

            if (user.getRole() == Role.HELPDESK)
                return "redirect:/helpdesk/dashboard";

            if (user.getRole() == Role.BANK_MANAGER)
                return "redirect:/bank-manager/dashboard";
        }

        auditService.recordFailedLogin(username, request);
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        auditService.recordLogout(user, session, request);
        session.invalidate();

        return "redirect:/login?logout=true";
    }
}
