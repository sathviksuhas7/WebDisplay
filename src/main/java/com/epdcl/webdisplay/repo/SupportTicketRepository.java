package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByCustomerCustomerId(Long customerId);

    List<SupportTicket> findByStatus(String status);

    List<SupportTicket> findByAssignedEmployeeEmployeeId(Long employeeId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}