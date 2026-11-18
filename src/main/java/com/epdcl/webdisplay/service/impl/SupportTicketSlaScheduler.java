package com.epdcl.webdisplay.service.impl;

import com.epdcl.webdisplay.entity.SupportTicket;
import com.epdcl.webdisplay.repo.SupportTicketRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SupportTicketSlaScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(SupportTicketSlaScheduler.class);

    private final SupportTicketRepository ticketRepository;

    public SupportTicketSlaScheduler(
            SupportTicketRepository ticketRepository) {

        this.ticketRepository = ticketRepository;
    }

    // Check overdue tickets every minute.
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void escalateOverdueTickets() {

        LocalDateTime now = LocalDateTime.now();

        List<SupportTicket> overdueTickets =
                ticketRepository.findTicketsForSlaEscalation(now);

        if (overdueTickets.isEmpty()) {
            return;
        }

        for (SupportTicket ticket : overdueTickets) {

            ticket.setEscalated(true);
            ticket.setEscalatedAt(now);

            logger.warn(
                    "SLA ESCALATION: Ticket ID {} | Priority: {} | Deadline: {} | Assigned Employee: {}",
                    ticket.getTicketId(),
                    ticket.getPriority(),
                    ticket.getSlaDeadline(),
                    ticket.getAssignedEmployee() == null
                            ? "Unassigned"
                            : ticket.getAssignedEmployee().getEmployeeName()
            );
        }

        ticketRepository.saveAll(overdueTickets);

        logger.info(
                "{} overdue support ticket(s) escalated.",
                overdueTickets.size()
        );
    }
}