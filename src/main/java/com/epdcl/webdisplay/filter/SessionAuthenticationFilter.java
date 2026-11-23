
package com.epdcl.webdisplay.filter;

import java.io.IOException;
import java.util.UUID;

import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.repo.UserRepository;
import com.epdcl.webdisplay.audit.AuditService;

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
    private final AuditService auditService;

    public SessionAuthenticationFilter(
            UserRepository userRepository,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return true;
        }

        String path = request.getServletPath();

        return path.equals("/")
                || path.equals("/login")
                || path.equals("/register")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico")
                || path.equals("/error")
                // Theme is a non-sensitive public setting used on the login/welcome pages too.
                || (path.equals("/api/theme")
                    && "GET".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Prevent protected pages and their redirects from being restored from browser cache.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

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

        // REJECT SESSIONS REPLACED BY A NEWER LOGIN
        if (!auditService.isCurrentSession(sessionUser, session)) {
            session.invalidate();
            rejectUnauthenticated(request, response, null);
            return;
        }

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

        /*
         * FORCE CUSTOMER PASSWORD CHANGE
         *
         * Customers with an initial password can access only
         * /change-password until they set a new password.
         */
        if ("CUSTOMER".equals(role)
                && currentUser.isMustChangePassword()
                && !path.equals("/change-password")
                && !path.equals("/api/session/heartbeat")) {

            if (path.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"message\":\"PASSWORD_CHANGE_REQUIRED\"}");
            } else {
                response.sendRedirect(
                        request.getContextPath() + "/change-password");
            }

            return;
        }

        // ONLY CUSTOMERS CAN USE THE PASSWORD CHANGE PAGE
        if (path.equals("/change-password")
                && !"CUSTOMER".equals(role)) {

            forwardAccessDenied(request, response);
            return;
        }

        // DEFAULT DENY FOR API REQUESTS
        boolean allowed = !path.startsWith("/api/");

        // Read-only site theme setting is available to every authenticated role.
        if (path.equals("/api/theme")
                && HttpMethod.GET.matches(request.getMethod())) {
            allowed = true;
        }

        // Session keep-alive / "am I still signed in?" check, used by every dashboard.
        if (path.equals("/api/session/heartbeat")) {
            allowed = true;
        }

        // API documentation is for administrators only.
        if (path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/")) {
            allowed = "ADMIN".equals(role);
        }

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

        // BANK MANAGER DASHBOARD
        else if (path.equals("/bank-manager")
                || path.startsWith("/bank-manager/")) {
            allowed = "BANK_MANAGER".equals(role);
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

        // ID DOCUMENT APIs
        else if (path.equals("/api/id-documents")
                || path.startsWith("/api/id-documents/")) {

            if ("EMPLOYEE".equals(role)) {

                allowed =
                        (HttpMethod.POST.matches(request.getMethod())
                                && path.equals("/api/id-documents/accounts"))
                                ||
                                (HttpMethod.GET.matches(request.getMethod())
                                        && path.equals("/api/id-documents/employee/rejected"))
                                ||
                                (HttpMethod.GET.matches(request.getMethod())
                                        && path.equals("/api/id-documents/employee/applications"))
                                ||
                                (HttpMethod.GET.matches(request.getMethod())
                                        && path.equals("/api/id-documents/employee/applications/search"))
                                || (HttpMethod.POST.matches(request.getMethod())
                                && path.matches("/api/id-documents/\\d+/reupload"))
                                ||
                                (HttpMethod.POST.matches(request.getMethod())
                                        && path.equals(
                                        "/api/id-documents/test/base64-validation"
                                ));
            } else if ("BANK_MANAGER".equals(role)) {

                // BANK MANAGER: ONLY DOCUMENT VERIFICATION APIs
                allowed =
                        (HttpMethod.GET.matches(request.getMethod())
                                && path.equals("/api/id-documents/manager/pending"))
                                ||
                                (HttpMethod.GET.matches(request.getMethod())
                                        && path.matches("/api/id-documents/manager/\\d+/file"))
                                ||
                                (HttpMethod.POST.matches(request.getMethod())
                                        && path.matches("/api/id-documents/manager/\\d+/approve"))
                                ||
                                (HttpMethod.POST.matches(request.getMethod())
                                        && path.matches("/api/id-documents/manager/\\d+/reject"))
                                ||
                                (HttpMethod.GET.matches(request.getMethod())
                                        && path.equals("/api/id-documents/manager/address-change/pending"))
                                ||
                                (HttpMethod.GET.matches(request.getMethod())
                                        && path.matches("/api/id-documents/manager/address-change/\\d+/proof"))
                                ||
                                (HttpMethod.POST.matches(request.getMethod())
                                        && path.matches("/api/id-documents/manager/address-change/\\d+/approve"))
                                ||
                                (HttpMethod.POST.matches(request.getMethod())
                                        && path.matches("/api/id-documents/manager/address-change/\\d+/reject"));
            }
        }

        // ACCOUNT AND TRANSACTION APIs
        else if (path.equals("/api/accounts")
                || path.startsWith("/api/accounts/")) {

            if ("EMPLOYEE".equals(role)) {
                allowed = !(HttpMethod.POST.matches(request.getMethod())
                        && path.matches("/api/accounts/?"));

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

                forwardAccessDenied(request, response);
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

    private void forwardAccessDenied(
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (response.isCommitted()) return;
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("accessDeniedMessage",
                "You are not authorized to access this page.");
        request.getRequestDispatcher("/WEB-INF/jsp/access-denied.jsp")
                .forward(request, response);
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
