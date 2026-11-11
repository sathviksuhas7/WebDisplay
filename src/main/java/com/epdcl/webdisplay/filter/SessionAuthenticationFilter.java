
package com.epdcl.webdisplay.filter;

import java.io.IOException;
import java.util.UUID;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.UserRepository;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SessionAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return true;
        }

        String path = request.getServletPath();

        return path.equals("/login")
                || path.equals("/register")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String path = request.getServletPath();

        // CHECK LOGIN
        if (session == null
                || !(session.getAttribute("loggedInUser") instanceof User)) {

            rejectUnauthenticated(request, response, session);
            return;
        }

        // GET USER FROM SESSION
        User sessionUser = (User) session.getAttribute("loggedInUser");

        // FETCH LATEST USER DETAILS FROM DATABASE
        User currentUser = userRepository.findById(sessionUser.getId())
                .orElse(null);

        String sessionRole = (String) session.getAttribute("role");

        // CHECK WHETHER USER EXISTS AND ROLE IS UNCHANGED
        if (currentUser == null
                || currentUser.getRole() == null
                || !currentUser.getRole().name().equals(sessionRole)) {

            session.invalidate();
            rejectUnauthenticated(request, response, null);
            return;
        }

        // REFRESH USER DETAILS IN SESSION
        session.setAttribute("loggedInUser", currentUser);
        session.setAttribute("role", currentUser.getRole().name());

        // CREATE CSRF TOKEN IF MISSING
        String csrfToken = (String) session.getAttribute("csrfToken");

        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
        }

        // VALIDATE CSRF TOKEN FOR STATE-CHANGING REQUESTS
        if (HttpMethod.POST.matches(request.getMethod())
                || HttpMethod.PUT.matches(request.getMethod())
                || HttpMethod.PATCH.matches(request.getMethod())
                || HttpMethod.DELETE.matches(request.getMethod())) {

            String requestToken = request.getHeader("X-CSRF-TOKEN");

            // Also support regular HTML form submissions
            if (requestToken == null) {
                requestToken = request.getParameter("csrfToken");
            }

            if (requestToken == null
                    || !csrfToken.equals(requestToken)) {

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"message\":\"INVALID_CSRF_TOKEN\"}");

                return;
            }
        }

        String role = currentUser.getRole().name();

        // DEFAULT DENY FOR API REQUESTS
        boolean allowed = !path.startsWith("/api/");

        // ADMIN DASHBOARD
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            allowed = "ADMIN".equals(role);
        }

        // EMPLOYEE DASHBOARD
        else if (path.equals("/employee")
                || path.startsWith("/employee/")) {
            allowed = "EMPLOYEE".equals(role);
        }

        // CUSTOMER DASHBOARD
        else if (path.equals("/customer")
                || path.startsWith("/customer/")) {
            allowed = "CUSTOMER".equals(role);
        }

        // HELPDESK DASHBOARD
        else if (path.equals("/helpdesk")
                || path.startsWith("/helpdesk/")) {
            allowed = "HELPDESK".equals(role);
        }

        // ADMIN MANAGEMENT APIs
        else if (path.equals("/api/admin")
                || path.startsWith("/api/admin/")) {
            allowed = "ADMIN".equals(role);
        }

        // HELPDESK APIs
        else if (path.equals("/api/helpdesk")
                || path.startsWith("/api/helpdesk/")) {
            allowed = "HELPDESK".equals(role);
        }

        // CUSTOMER APIs
        else if (path.equals("/api/customer")
                || path.startsWith("/api/customer/")) {
            allowed = "CUSTOMER".equals(role);
        }

        // CUSTOMER MANAGEMENT APIs
        else if (path.equals("/api/customers")
                || path.startsWith("/api/customers/")) {
            allowed = "EMPLOYEE".equals(role);
        }


        // ACCOUNT AND TRANSACTION APIs
        else if (path.equals("/api/accounts")
                || path.startsWith("/api/accounts/")) {

            if ("EMPLOYEE".equals(role)) {
                allowed = true;
            } else if ("CUSTOMER".equals(role)) {
                allowed = path.startsWith("/api/accounts/recipient/")
                        || path.matches("/api/accounts/[^/]+/transfer");
            }
        }

        // REJECT UNAUTHORIZED ACCESS
        if (!allowed) {

            if (path.startsWith("/api/")) {

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"message\":\"ACCESS_DENIED\"}");

            } else {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "You are not authorized to access this page.");
            }

            return;
        }

        // PREVENT CACHING OF AUTHENTICATED PAGES
        response.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate");

        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        filterChain.doFilter(request, response);
    }

    // HANDLE EXPIRED OR INVALID SESSIONS
    private void rejectUnauthenticated(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session) throws IOException {

        if (session != null) {
            session.invalidate();
        }

        String path = request.getServletPath();

        if (path.startsWith("/api/")) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(
                    "{\"message\":\"SESSION_EXPIRED\"}");

        } else {

            response.sendRedirect(
                    request.getContextPath() + "/login?expired=true");
        }
    }
}
