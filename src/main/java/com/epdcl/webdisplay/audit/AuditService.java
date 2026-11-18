package com.epdcl.webdisplay.audit;

import com.epdcl.webdisplay.entity.AuditLog;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.entity.UserActiveSession;

import com.epdcl.webdisplay.repo.AuditLogRepository;
import com.epdcl.webdisplay.repo.UserActiveSessionRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class AuditService {

    private final AuditLogRepository auditRepository;
    private final UserActiveSessionRepository sessionRepository;

    public AuditService(
            AuditLogRepository auditRepository,
            UserActiveSessionRepository sessionRepository) {

        this.auditRepository = auditRepository;
        this.sessionRepository = sessionRepository;
    }


    @Transactional
    public void registerLogin(
            User user,
            HttpSession session,
            HttpServletRequest request) {

        // Check whether a session record already exists
        var existingSession =
                sessionRepository.findByUserId(user.getId());

        // Record replacement only if an existing session is active
        if (existingSession.isPresent()
                && existingSession.get().isActive()) {

            saveLog(
                    user,
                    "SESSION_REPLACED",
                    "Previous session replaced by a new successful login.",
                    request
            );
        }

        // Reuse existing record or create a new one
        UserActiveSession activeSession =
                existingSession.orElseGet(UserActiveSession::new);

        activeSession.setUserId(user.getId());
        activeSession.setSessionIdHash(hash(session.getId()));
        activeSession.setActive(true);
        activeSession.setCreatedAt(LocalDateTime.now());
        activeSession.setLastActivityAt(LocalDateTime.now());

        sessionRepository.save(activeSession);

        saveLog(
                user,
                "LOGIN",
                "User logged in successfully.",
                request
        );
    }


    @Transactional
    public void recordLogout(
            User user,
            HttpSession session,
            HttpServletRequest request) {

        if (user == null || session == null) {
            return;
        }

        UserActiveSession activeSession =
                sessionRepository.findByUserId(user.getId())
                        .orElse(null);

        if (activeSession == null) {
            return;
        }

        String currentHash = hash(session.getId());

        if (activeSession.isActive()
                && activeSession.getSessionIdHash().equals(currentHash)) {

            activeSession.setActive(false);
            activeSession.setLastActivityAt(LocalDateTime.now());

            sessionRepository.save(activeSession);

            saveLog(
                    user,
                    "LOGOUT",
                    "User logged out successfully.",
                    request
            );
        }
    }

    @Transactional
    public void recordFailedLogin(
            String username,
            HttpServletRequest request) {

        AuditLog log = new AuditLog();

        log.setUsername(
                username == null || username.isBlank()
                        ? "UNKNOWN"
                        : username.trim()
        );

        log.setAction("FAILED_LOGIN");
        log.setDetails("Invalid login credentials.");

        setRequestDetails(log, request);

        auditRepository.save(log);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentSession(
            User user,
            HttpSession session) {

        if (user == null || session == null) {
            return false;
        }

        UserActiveSession activeSession =
                sessionRepository.findByUserId(user.getId())
                        .orElse(null);

        return activeSession != null
                && activeSession.isActive()
                && activeSession.getSessionIdHash()
                .equals(hash(session.getId()));
    }

    @Transactional
    public void touchCurrentSession(
            User user,
            HttpSession session) {

        if (user == null || session == null) {
            return;
        }

        sessionRepository.findByUserId(user.getId())
                .ifPresent(activeSession -> {

                    if (activeSession.isActive()
                            && activeSession.getSessionIdHash()
                            .equals(hash(session.getId()))) {

                        activeSession.setLastActivityAt(
                                LocalDateTime.now()
                        );

                        sessionRepository.save(activeSession);
                    }
                });
    }

    @Transactional
    public void recordActivity(
            User user,
            String action,
            String details,
            HttpServletRequest request) {

        if (user == null) {
            return;
        }

        saveLog(user, action, details, request);
    }

    private void saveLog(
            User user,
            String action,
            String details,
            HttpServletRequest request) {

        AuditLog log = new AuditLog();

        log.setUserId(user.getId());
        log.setUsername(user.getUsername());
        log.setRole(user.getRole().name());
        log.setAction(action);
        log.setDetails(details);

        setRequestDetails(log, request);

        auditRepository.save(log);
    }

    private void setRequestDetails(
            AuditLog log,
            HttpServletRequest request) {

        if (request == null) {
            return;
        }

        log.setIpAddress(request.getRemoteAddr());

        String userAgent = request.getHeader("User-Agent");

        if (userAgent != null) {
            log.setUserAgent(
                    userAgent.substring(
                            0,
                            Math.min(userAgent.length(), 500)
                    )
            );
        }
    }

    private String hash(String sessionId) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                    sessionId.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(bytes);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "Unable to hash session ID",
                    exception
            );
        }
    }
}