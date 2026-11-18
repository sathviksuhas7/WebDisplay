
package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    @Query("""
    SELECT t FROM SupportTicket t
    WHERE t.escalated = false
      AND t.slaDeadline IS NOT NULL
      AND t.slaDeadline < :currentTime
      AND UPPER(t.status) NOT IN ('CLOSED', 'RESOLVED')
    """)
    List<SupportTicket> findTicketsForSlaEscalation(
            @Param("currentTime") LocalDateTime currentTime);

    List<SupportTicket> findByCustomerCustomerId(Long customerId);

    List<SupportTicket> findByStatus(String status);

    List<SupportTicket> findByPriority(String priority);

    List<SupportTicket> findByStatusAndPriority(
            String status, String priority);

    List<SupportTicket> findByAssignedEmployeeEmployeeId(Long employeeId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}
