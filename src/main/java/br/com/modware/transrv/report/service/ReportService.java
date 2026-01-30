package br.com.modware.transrv.report.service;

import org.springframework.stereotype.Service;

import br.com.modware.transrv.report.dto.GenerateReportRequest;
import br.com.modware.transrv.report.dto.ReportData;
import br.com.modware.transrv.report.dto.ReportFormat;
import br.com.modware.transrv.report.port.GenerateReportPort;
import br.com.modware.transrv.report.port.GetDataPort;

@Service
public class ReportService {

    private final GenerateReportPort generateReportPort;
    private final GetDataPort getDataPort;

    public ReportService(GenerateReportPort generateReportPort, GetDataPort getDataPort) {
        this.generateReportPort = generateReportPort;
        this.getDataPort = getDataPort;
    }

    public byte[] generateReport(GenerateReportRequest request, ReportFormat format) {
        ReportData reportData = getDataPort.getData(request);
        return generateReportPort.generateReport(reportData, format);
    }
}
