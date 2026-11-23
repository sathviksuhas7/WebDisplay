
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
    public String showLoginPage(HttpSession session, HttpServletResponse response, Model model) {
        noStore(response);

        // Already signed in with a valid session: go straight to the dashboard
        // instead of showing the login form again.
        if (session.getAttribute("loggedInUser") instanceof User user
                && auditService.isCurrentSession(user, session)) {
            return redirectFor(user);
        }

        // Keep one CSRF token for the anonymous login session. Repeated GETs
        // (DevTools, another tab or browser prefetch) must not invalidate forms.
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }

        Object loginError = session.getAttribute("loginError");
        Object loginUsername = session.getAttribute("loginUsername");
        if (loginError != null) {
            model.addAttribute("error", loginError);
            session.removeAttribute("loginError");
        }
        if (loginUsername != null) {
            model.addAttribute("username", loginUsername);
            session.removeAttribute("loginUsername");
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
            HttpServletResponse response) {

        noStore(response);

        String sessionToken = (String) session.getAttribute("csrfToken");

        if (sessionToken == null) {
            // A session can be recreated after an expired browser session. Issue
            // a fresh token and use PRG so the browser never keeps a stale form.
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
            session.setAttribute("loginError",
                    "Your login page has expired. Please try again.");
            session.setAttribute("loginUsername", username);
            return "redirect:/login?csrfExpired=true";
        }

        if (!sessionToken.equals(csrfToken)) {
            // Do not rotate a valid session token just because one old tab
            // submitted a stale token; doing so would invalidate other open tabs.
            session.setAttribute("loginError",
                    "Your login page has expired. Please refresh and try again.");
            session.setAttribute("loginUsername", username);
            return "redirect:/login?csrfExpired=true";
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
        // Post/Redirect/Get prevents browser Back from replaying old failed-login
        // POST pages. Keep the username, but never retain the password.
        session.setAttribute("loginError", "Invalid username or password.");
        session.setAttribute("loginUsername", username);
        return "redirect:/login?failed=true";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpSession session, HttpServletResponse response) {

        noStore(response);
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
