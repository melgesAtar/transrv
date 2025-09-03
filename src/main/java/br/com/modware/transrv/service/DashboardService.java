package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Ticket;
import br.com.modware.transrv.repository.TicketRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Summary getSummary() {
        Summary s = new Summary();
        s.setOpen(ticketRepository.countByStatus(Ticket.Status.OPEN));
        s.setClosed(ticketRepository.countByStatus(Ticket.Status.CLOSED));
        s.setClosedWithoutSolution(ticketRepository.countByStatus(Ticket.Status.CLOSED_WITHOUT_SOLUTION));
        s.setOpenedToday(ticketRepository.countOpenedToday(Ticket.Status.OPEN));
        s.setRecent(ticketRepository.findTop20ByOrderByCreatedAtDesc());
        return s;
    }

    @Data
    public static class Summary {
        private long open;
        private long closed;
        private long closedWithoutSolution;
        private long openedToday;
        private List<Ticket> recent;
    }
}


