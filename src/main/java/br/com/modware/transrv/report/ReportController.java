package br.com.modware.transrv.report;

import br.com.modware.transrv.report.dto.GenerateReportRequest;
import br.com.modware.transrv.report.dto.ReportFormat;
import br.com.modware.transrv.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints para geração de relatórios")
@SecurityRequirement(name = "JWT")
public class ReportController {

    private static final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Gerar relatório", description = "Gera um relatório completo com dados do período informado. Aceita formato PDF ou DOCX via parâmetro 'format'. Retorna o arquivo para download.")
    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateReport(
            @Parameter(description = "Data/hora inicial do período (opcional, padrão: 30 dias atrás)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Data/hora final do período (opcional, padrão: agora)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Formato do documento: pdf ou docx (padrão: pdf)")
            @RequestParam(required = false, defaultValue = "pdf") String format) {
        ReportFormat reportFormat = ReportFormat.fromString(format);
        GenerateReportRequest request = new GenerateReportRequest(startDate, endDate);
        byte[] report = reportService.generateReport(request, reportFormat);

        HttpHeaders headers = new HttpHeaders();
        if (reportFormat == ReportFormat.DOCX) {
            headers.setContentType(MediaType.parseMediaType(CONTENT_TYPE_DOCX));
            headers.setContentDispositionFormData("attachment", "relatorio-chamados.docx");
        } else {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "relatorio-chamados.pdf");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
    }
}
