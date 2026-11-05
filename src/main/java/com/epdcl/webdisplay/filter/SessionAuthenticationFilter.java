package com.epdcl.webdisplay.filter;

import java.io.IOException;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        // Allow internal JSP forwards and error dispatches
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

        boolean loggedIn = session != null
                && session.getAttribute("loggedInUser") != null;

        if (!loggedIn) {

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

            return;
        }

        response.setHeader(
                "Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        filterChain.doFilter(request, response);
    }
}