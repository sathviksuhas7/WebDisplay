
package com.epdcl.webdisplay.audit;

import com.epdcl.webdisplay.audit.annotation.Audited;
import com.epdcl.webdisplay.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(
            pointcut = "@annotation(audited)",
            argNames = "joinPoint,audited"
    )
    public void auditSuccessfulOperation(
            JoinPoint joinPoint,
            Audited audited) {

        // Get the current HTTP request
        if (!(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes)) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // Get the existing session
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        // Retrieve the logged-in user from your existing session
        Object sessionValue = session.getAttribute("loggedInUser");
        if (!(sessionValue instanceof User user)) {
            return;
        }

        // Record the successful business operation
        auditService.recordActivity(
                user,
                audited.action(),
                audited.details(),
                request
        );
    }
}
