package br.com.modware.transrv.service;

import br.com.modware.transrv.model.TicketNotification;
import br.com.modware.transrv.repository.TicketNotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class TicketNotificationService {
    private final TicketNotificationRepository ticketNotificationRepository;

    public TicketNotificationService(TicketNotificationRepository ticketNotificationRepository) {
        this.ticketNotificationRepository = ticketNotificationRepository;
    }

    public void save(TicketNotification notification) {
        ticketNotificationRepository.save(notification);
    }
}
