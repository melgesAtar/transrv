package br.com.modware.transrv.report.adapter;

import br.com.modware.transrv.model.Ticket;
import br.com.modware.transrv.report.dto.*;
import br.com.modware.transrv.report.port.GetDataPort;
import br.com.modware.transrv.repository.TicketRepository;
import br.com.modware.transrv.repository.WAMessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class GetDataAdapter implements GetDataPort {

    private static final int TOP_LIMIT = 10;

    private final TicketRepository ticketRepository;
    private final WAMessageRepository waMessageRepository;
    private final EntityManager entityManager;

    public GetDataAdapter(TicketRepository ticketRepository,
                          WAMessageRepository waMessageRepository,
                          EntityManager entityManager) {
        this.ticketRepository = ticketRepository;
        this.waMessageRepository = waMessageRepository;
        this.entityManager = entityManager;
    }

    @Override
    public ReportData getData(GenerateReportRequest request) {
        LocalDateTime startDate = request.startDate();
        LocalDateTime endDate = request.endDate();
        LocalDateTime generatedAt = LocalDateTime.now();

        long openedInPeriod = ticketRepository.countTotalTicketsBetween(startDate, endDate);
        long closedWithSolution = ticketRepository.countClosedTicketsBetween(startDate, endDate, Ticket.Status.CLOSED);
        long closedWithoutSolution = ticketRepository.countClosedWithoutSolutionTicketsBetween(startDate, endDate, Ticket.Status.CLOSED_WITHOUT_SOLUTION);
        long totalMessages = waMessageRepository.countBySentAtBetween(startDate, endDate);

        List<EmployeeCount> topEmployeesOpened = findTopEmployeesByOpened(startDate, endDate);
        List<EmployeeCount> topEmployeesClosed = findTopEmployeesByClosed(startDate, endDate);
        List<GroupAvgTime> avgResponseTimeByGroup = findAvgResponseTimeByGroup(startDate, endDate);
        List<GroupAlertCount> alertsByGroup = findAlertsByGroup(startDate, endDate);
        List<AlertCount> topAlertTermsGeneral = findTopAlertTermsGeneral(startDate, endDate);

        return new ReportData(
                startDate, endDate, generatedAt,
                openedInPeriod, closedWithSolution, closedWithoutSolution, totalMessages,
                topEmployeesOpened, topEmployeesClosed,
                avgResponseTimeByGroup, alertsByGroup, topAlertTermsGeneral
        );
    }

    /** Uma query com JOIN: retorna nome e contagem, sem N+1. */
    @SuppressWarnings("unchecked")
    private List<EmployeeCount> findTopEmployeesByOpened(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT e.name, COUNT(t.id) as cnt
            FROM ticket t
            INNER JOIN employee e ON t.employee_open_id = e.id
            WHERE t.created_at >= :start AND t.created_at <= :end
            GROUP BY t.employee_open_id
            ORDER BY cnt DESC
            """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setMaxResults(TOP_LIMIT);
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> new EmployeeCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<EmployeeCount> findTopEmployeesByClosed(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT e.name, COUNT(t.id) as cnt
            FROM ticket t
            INNER JOIN employee e ON t.employee_close_id = e.id
            WHERE t.closed_at >= :start AND t.closed_at <= :end
            GROUP BY t.employee_close_id
            ORDER BY cnt DESC
            """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setMaxResults(TOP_LIMIT);
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> new EmployeeCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<GroupAvgTime> findAvgResponseTimeByGroup(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT g.group_name, AVG(TIMESTAMPDIFF(SECOND, t.created_at, t.closed_at)) as avg_sec
            FROM ticket t
            INNER JOIN wa_group g ON t.wa_group_id = g.id
            WHERE t.created_at >= :start AND t.created_at <= :end AND t.closed_at IS NOT NULL
            GROUP BY t.wa_group_id
            """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    String groupName = row[0] != null ? (String) row[0] : "N/A";
                    double avgSec = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
                    return new GroupAvgTime(groupName, formatDuration((long) avgSec));
                })
                .toList();
    }

    /** Uma query com JOIN: retorna group_name, alert name e contagem; agrupamento por grupo em memória. */
    @SuppressWarnings("unchecked")
    private List<GroupAlertCount> findAlertsByGroup(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT g.group_name, a.name as alert_name, COUNT(t.id) as cnt
            FROM ticket t
            INNER JOIN wa_group g ON t.wa_group_id = g.id
            INNER JOIN alert_term a ON t.alert_term_id = a.id
            WHERE t.created_at >= :start AND t.created_at <= :end
            GROUP BY t.wa_group_id, t.alert_term_id
            ORDER BY t.wa_group_id, cnt DESC
            """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        List<Object[]> rows = query.getResultList();
        Map<String, List<AlertCount>> byGroupName = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String groupName = row[0] != null ? (String) row[0] : "N/A";
            String alertName = row[1] != null ? (String) row[1] : "N/A";
            long cnt = ((Number) row[2]).longValue();
            byGroupName.computeIfAbsent(groupName, k -> new ArrayList<>()).add(new AlertCount(alertName, cnt));
        }
        return byGroupName.entrySet().stream()
                .map(e -> new GroupAlertCount(e.getKey(), e.getValue()))
                .toList();
    }

    /** Uma query com JOIN: retorna nome do termo e contagem. */
    @SuppressWarnings("unchecked")
    private List<AlertCount> findTopAlertTermsGeneral(LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT a.name, COUNT(t.id) as cnt
            FROM ticket t
            INNER JOIN alert_term a ON t.alert_term_id = a.id
            WHERE t.created_at >= :start AND t.created_at <= :end
            GROUP BY t.alert_term_id
            ORDER BY cnt DESC
            """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setMaxResults(TOP_LIMIT);
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> new AlertCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    private static String formatDuration(long totalSeconds) {
        if (totalSeconds < 0) return "0 min";
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours > 0) {
            return String.format("%d h e %d min", hours, minutes);
        }
        return String.format("%d min", minutes);
    }
}
