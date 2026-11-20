
package com.epdcl.webdisplay.controller;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.Role;
import com.epdcl.webdisplay.service.impl.UserService;
import com.epdcl.webdisplay.audit.AuditService;

import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public String showLoginPage(HttpSession session, HttpServletResponse response) {
        noStore(response);

        // Already signed in with a valid session: go straight to the dashboard
        // instead of showing the login form again.
        if (session.getAttribute("loggedInUser") instanceof User user
                && auditService.isCurrentSession(user, session)) {
            return redirectFor(user);
        }

        // Reuse the existing login token. Re-generating it on every GET broke
        // login whenever the browser fetched /login twice (DevTools open,
        // a second tab, prefetch) because the form then held a stale token.
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
        return "login";
    }

    private static void noStore(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private static String redirectFor(User user) {
        if (user.getRole() == Role.CUSTOMER && user.isMustChangePassword()) {
            return "redirect:/change-password";
        }
        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case EMPLOYEE -> "redirect:/employee/dashboard";
            case CUSTOMER -> "redirect:/customer/dashboard";
            case HELPDESK -> "redirect:/helpdesk/dashboard";
            case BANK_MANAGER -> "redirect:/bank-manager/dashboard";
            default -> "redirect:/login";
        };
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String csrfToken,
            HttpServletRequest request,
            HttpSession session,
            Model model,
            HttpServletResponse response) {

        noStore(response);

        String sessionToken = (String) session.getAttribute("csrfToken");

        if (sessionToken == null || !sessionToken.equals(csrfToken)) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
            model.addAttribute("error",
                    "Your login page had expired. Please enter your details again.");
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

            return redirectFor(user);
        }

        auditService.recordFailedLogin(username, request);
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpSession session, HttpServletResponse response) {

        noStore(response);
        // Tell the browser to drop cached copies of pages (including the
        // back/forward cache) so "Back" cannot show the old dashboard.
        response.setHeader("Clear-Site-Data", "\"cache\"");

        User user = (User) session.getAttribute("loggedInUser");

        // Logout must complete even if audit persistence has a temporary problem.
        // The server-side session is always invalidated and the browser cookie is expired.
        try {
            if (user != null) {
                auditService.recordLogout(user, session, request);
            }
        } finally {
            try {
                session.invalidate();
            } finally {
                Cookie sessionCookie = new Cookie("JSESSIONID", "");
                String contextPath = request.getContextPath();
                sessionCookie.setPath(contextPath == null || contextPath.isEmpty() ? "/" : contextPath);
                sessionCookie.setMaxAge(0);
                sessionCookie.setHttpOnly(true);
                sessionCookie.setSecure(request.isSecure());
                response.addCookie(sessionCookie);
            }
        }

        return "redirect:/login?logout=true";
    }
}
