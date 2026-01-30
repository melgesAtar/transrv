package br.com.modware.transrv.report.port;

import br.com.modware.transrv.report.dto.ReportData;
import br.com.modware.transrv.report.dto.ReportFormat;

public interface GenerateReportPort {

    byte[] generateReport(ReportData data, ReportFormat format);

}