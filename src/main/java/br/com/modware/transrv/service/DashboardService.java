package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.dashboard.DashboardSummaryDTO;
import br.com.modware.transrv.model.Ticket;
import br.com.modware.transrv.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        DashboardSummaryDTO s = new DashboardSummaryDTO();
        java.time.ZoneId zone = java.time.ZoneId.of("America/Sao_Paulo");
        java.time.LocalDateTime last24h = java.time.ZonedDateTime.now(zone).minusHours(24).toLocalDateTime();
        s.setOpen(ticketRepository.countOpenedSince(Ticket.Status.OPEN, last24h));
        s.setClosed(ticketRepository.countClosedSince(Ticket.Status.CLOSED, last24h));
        s.setClosedWithoutSolution(ticketRepository.countClosedSince(Ticket.Status.CLOSED_WITHOUT_SOLUTION, last24h));
        java.time.LocalDate today = java.time.LocalDate.now(zone);
        java.time.LocalDateTime startOfDay = today.atStartOfDay(zone).toLocalDateTime();
        s.setOpenedToday(ticketRepository.countCreatedSince(startOfDay));

        List<Ticket> recent = ticketRepository.findRecent(org.springframework.data.domain.PageRequest.of(0, 20));
        // Contadores por nível (apenas tickets Abertos)
        long level1 = 0;
        long level2 = 0;
        long level3 = 0;
        for (Ticket t : recent) {
            if (t.getStatus() == Ticket.Status.OPEN) {
                Integer lvl = t.getCurrentEscalationLevel();
                if (lvl != null) {
                    if (lvl == 1) level1++;
                    else if (lvl == 2) level2++;
                    else if (lvl >= 3) level3++;
                }
            }
        }
        s.setOpenLevel1(level1);
        s.setOpenLevel2(level2);
        s.setOpenLevel3(level3);
        List<DashboardSummaryDTO.RecentTicketDTO> mapped = recent.stream().map(t -> {
            DashboardSummaryDTO.RecentTicketDTO dto = new DashboardSummaryDTO.RecentTicketDTO();
            dto.setId(t.getId());
            dto.setGroupName(t.getWaGroup() != null ? t.getWaGroup().getGroupName() : null);
            dto.setAlertCode(t.getAlertTerm() != null ? t.getAlertTerm().getCode() : null);
            dto.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
            dto.setCurrentEscalationLevel(t.getCurrentEscalationLevel());
            dto.setCreatedAt(t.getCreatedAt());
            dto.setClosedAt(t.getClosedAt());
            if (t.getMessageResponsibleForOpeningTheCall() != null) {
                dto.setOpeningMessageContent(t.getMessageResponsibleForOpeningTheCall().getMessageContent());
            }
            dto.setOpenedIncorrectly(t.getOpenedIncorrectly());
            return dto;
        }).collect(Collectors.toList());
        s.setRecent(mapped);
        return s;
    }
}


