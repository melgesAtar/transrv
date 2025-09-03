package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO;
import br.com.modware.transrv.model.Ticket;
import br.com.modware.transrv.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public DashboardSummaryDTO getSummary() {
        DashboardSummaryDTO s = new DashboardSummaryDTO();
        s.setOpen(ticketRepository.countByStatus(Ticket.Status.OPEN));
        s.setClosed(ticketRepository.countByStatus(Ticket.Status.CLOSED));
        s.setClosedWithoutSolution(ticketRepository.countByStatus(Ticket.Status.CLOSED_WITHOUT_SOLUTION));
        s.setOpenedToday(ticketRepository.countOpenedToday(Ticket.Status.OPEN));

        List<Ticket> recent = ticketRepository.findTop20ByOrderByCreatedAtDesc();
        List<DashboardSummaryDTO.RecentTicketDTO> mapped = recent.stream().map(t -> {
            DashboardSummaryDTO.RecentTicketDTO dto = new DashboardSummaryDTO.RecentTicketDTO();
            dto.setId(t.getId());
            dto.setGroupName(t.getWaGroup() != null ? t.getWaGroup().getGroupName() : null);
            dto.setAlertCode(t.getAlertTerm() != null ? t.getAlertTerm().getCode() : null);
            dto.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
            dto.setCurrentEscalationLevel(t.getCurrentEscalationLevel());
            dto.setCreatedAt(t.getCreatedAt());
            dto.setClosedAt(t.getClosedAt());
            return dto;
        }).collect(Collectors.toList());
        s.setRecent(mapped);
        return s;
    }
}


