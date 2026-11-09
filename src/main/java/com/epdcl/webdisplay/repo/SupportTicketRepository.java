
package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByCustomerCustomerId(Long customerId);

    List<SupportTicket> findByStatus(String status);

    List<SupportTicket> findByPriority(String priority);

    List<SupportTicket> findByStatusAndPriority(
            String status, String priority);

    List<SupportTicket> findByAssignedEmployeeEmployeeId(Long employeeId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}
