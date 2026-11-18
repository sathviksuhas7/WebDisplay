package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.TicketConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketConversationMessageRepository
        extends JpaRepository<TicketConversationMessage, Long> {

    List<TicketConversationMessage>
    findByTicketTicketIdOrderByCreatedAtAscMessageIdAsc(Long ticketId);
}