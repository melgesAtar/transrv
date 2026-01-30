package br.com.modware.transrv.report.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReportData(
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime generatedAt,
        long openedInPeriod,
        long closedWithSolution,
        long closedWithoutSolution,
        long totalMessages,
        List<EmployeeCount> topEmployeesOpened,
        List<EmployeeCount> topEmployeesClosed,
        List<GroupAvgTime> avgResponseTimeByGroup,
        List<GroupAlertCount> alertsByGroup,
        List<AlertCount> topAlertTermsGeneral
) {
}
