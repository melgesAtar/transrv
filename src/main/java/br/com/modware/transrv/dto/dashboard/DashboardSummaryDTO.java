package br.com.modware.transrv.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardSummaryDTO {
    private long open;
    private long closed;
    private long closedWithoutSolution;
    private long openedToday;
    private List<RecentTicketDTO> recent;

    @Data
    public static class RecentTicketDTO {
        private Long id;
        private String groupName;
        private String alertCode;
        private String status;
        private Integer currentEscalationLevel;
        private LocalDateTime createdAt;
        private LocalDateTime closedAt;
    }
}


