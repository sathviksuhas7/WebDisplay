package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.AuditLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:username IS NULL OR
               LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:role IS NULL OR a.role = :role)
        AND (:action IS NULL OR a.action = :action)
        AND (:fromDate IS NULL OR a.createdAt >= :fromDate)
        AND (:toDate IS NULL OR a.createdAt <= :toDate)
        ORDER BY a.createdAt DESC
    """)
    Page<AuditLog> search(
            @Param("username") String username,
            @Param("role") String role,
            @Param("action") String action,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}