package br.com.modware.transrv.report.port;

import br.com.modware.transrv.report.dto.GenerateReportRequest;
import br.com.modware.transrv.report.dto.ReportData;

public interface GetDataPort {

    ReportData getData(GenerateReportRequest request);
} 