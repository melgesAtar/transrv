package br.com.modware.transrv.report.dto;

import java.time.LocalDateTime;

import org.springframework.data.repository.query.Param;

public record GenerateReportRequest(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate) {
    public GenerateReportRequest {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
    }
} 
