package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.TicketRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRemarkRepository
        extends JpaRepository<TicketRemark, Long> {

    List<TicketRemark> findByTicketTicketIdOrderByCreatedAtDesc(Long ticketId);
}