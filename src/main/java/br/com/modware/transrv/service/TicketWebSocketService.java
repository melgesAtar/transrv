package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.ticket.TicketEvent;
import br.com.modware.transrv.model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TicketWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(TicketWebSocketService.class);
    private static final String TICKETS_TOPIC = "/topic/tickets";
    private static final String TICKETS_ALERTS_TOPIC = "/topic/tickets/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public TicketWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Publica evento de ticket aberto para todos os clientes conectados
     */
    public void publishTicketOpened(Ticket ticket) {
        try {
            TicketEvent event = TicketEvent.opened(ticket);
            messagingTemplate.convertAndSend(TICKETS_TOPIC, event);
            log.debug("Evento TICKET_OPENED publicado: ticketId={}", ticket.getId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento TICKET_OPENED para ticketId={}", ticket.getId(), e);
        }
    }

    /**
     * Publica evento de ticket fechado para todos os clientes conectados
     */
    public void publishTicketClosed(Ticket ticket) {
        try {
            TicketEvent event = TicketEvent.closed(ticket);
            messagingTemplate.convertAndSend(TICKETS_TOPIC, event);
            log.debug("Evento TICKET_CLOSED publicado: ticketId={}", ticket.getId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento TICKET_CLOSED para ticketId={}", ticket.getId(), e);
        }
    }

    /**
     * Publica evento de ticket atualizado para todos os clientes conectados
     */
    public void publishTicketUpdated(Ticket ticket) {
        try {
            TicketEvent event = TicketEvent.updated(ticket);
            messagingTemplate.convertAndSend(TICKETS_TOPIC, event);
            log.debug("Evento TICKET_UPDATED publicado: ticketId={}", ticket.getId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento TICKET_UPDATED para ticketId={}", ticket.getId(), e);
        }
    }

    /**
     * Publica evento de ticket escalado para todos os clientes conectados
     */
    public void publishTicketEscalated(Ticket ticket) {
        try {
            TicketEvent event = TicketEvent.escalated(ticket);
            messagingTemplate.convertAndSend(TICKETS_TOPIC, event);
            messagingTemplate.convertAndSend(TICKETS_ALERTS_TOPIC, event);
            log.debug("Evento TICKET_ESCALATED publicado: ticketId={}, level={}", 
                ticket.getId(), ticket.getCurrentEscalationLevel());
        } catch (Exception e) {
            log.error("Erro ao publicar evento TICKET_ESCALATED para ticketId={}", ticket.getId(), e);
        }
    }

    /**
     * Publica evento de ticket marcado como incorreto para todos os clientes conectados
     */
    public void publishTicketMarkedIncorrect(Ticket ticket) {
        try {
            TicketEvent event = TicketEvent.markedIncorrect(ticket);
            messagingTemplate.convertAndSend(TICKETS_TOPIC, event);
            log.debug("Evento TICKET_MARKED_INCORRECT publicado: ticketId={}", ticket.getId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento TICKET_MARKED_INCORRECT para ticketId={}", ticket.getId(), e);
        }
    }
}
