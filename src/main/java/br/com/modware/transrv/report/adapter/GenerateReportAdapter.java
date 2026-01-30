package br.com.modware.transrv.report.adapter;

import br.com.modware.transrv.report.dto.*;
import br.com.modware.transrv.report.port.GenerateReportPort;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GenerateReportAdapter implements GenerateReportPort {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_TIME_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    @Override
    public byte[] generateReport(ReportData reportData, ReportFormat format) {
        return switch (format) {
            case DOCX -> generateDocx(reportData);
            case PDF -> generatePdf(reportData);
        };
    }

    private byte[] generatePdf(ReportData reportData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            pdf.getDocumentInfo().setTitle("Relatório TRANSRV");
            pdf.getDocumentInfo().setAuthor("Transrv");
            pdf.getDocumentInfo().setSubject("Relatório completo do período");

            addHeaderPdf(document, reportData);
            addSection1ResumoGeralPdf(document, reportData);
            addSection2FuncionariosMaisAbrirPdf(document, reportData);
            addSection3FuncionariosMaisFecharPdf(document, reportData);
            addSection4TempoMedioPorGrupoPdf(document, reportData);
            addSection5AlertasPorGrupoPdf(document, reportData);
            addSection6ResumoPorStatusPdf(document, reportData);
            addSection7ResumoPorTermoAlertaPdf(document, reportData);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar PDF do relatório", e);
        }
        return baos.toByteArray();
    }

    private byte[] generateDocx(ReportData reportData) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            addHeaderDocx(doc, reportData);
            addSection1ResumoGeralDocx(doc, reportData);
            addSection2FuncionariosMaisAbrirDocx(doc, reportData);
            addSection3FuncionariosMaisFecharDocx(doc, reportData);
            addSection4TempoMedioPorGrupoDocx(doc, reportData);
            addSection5AlertasPorGrupoDocx(doc, reportData);
            addSection6ResumoPorStatusDocx(doc, reportData);
            addSection7ResumoPorTermoAlertaDocx(doc, reportData);

            doc.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar DOCX do relatório", e);
        }
    }

    // --- PDF ---
    private void addHeaderPdf(Document document, ReportData reportData) {
        document.add(new Paragraph("RELATÓRIO TRANSRV")
                .setFontSize(22)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph(String.format("Período: %s a %s.",
                        reportData.startDate().format(DATE_TIME_FORMATTER),
                        reportData.endDate().format(DATE_TIME_FORMATTER)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Gerado em: " + reportData.generatedAt().format(DATE_TIME_FULL) + ".")
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n\n"));
    }

    private void addSection1ResumoGeralPdf(Document document, ReportData reportData) {
        addSectionTitlePdf(document, "1. Resumo geral do período");
        String text = String.format(
                "No período foram abertos %d chamados. Foram fechados %d chamados com solução e %d chamados fechados sem solução. O total de mensagens trocadas no período foi de %d.",
                reportData.openedInPeriod(), reportData.closedWithSolution(), reportData.closedWithoutSolution(), reportData.totalMessages());
        document.add(new Paragraph(text).setFontSize(11));
        document.add(new Paragraph("\n"));
    }

    private void addSection2FuncionariosMaisAbrirPdf(Document document, ReportData reportData) {
        addSectionTitlePdf(document, "2. Funcionários que mais abriram chamados");
        List<EmployeeCount> list = reportData.topEmployeesOpened();
        String text = list.isEmpty() ? "Não há dados de funcionários que abriram chamados no período."
                : "Os funcionários que mais abriram chamados no período foram: "
                + list.stream().map(e -> String.format("%s com %d chamados", e.employeeName(), e.count())).collect(Collectors.joining(", "))
                + ", conforme os dados do sistema.";
        document.add(new Paragraph(text).setFontSize(11));
        document.add(new Paragraph("\n"));
    }

    private void addSection3FuncionariosMaisFecharPdf(Document document, ReportData reportData) {
        addSectionTitlePdf(document, "3. Funcionários que mais fecharam chamados");
        List<EmployeeCount> list = reportData.topEmployeesClosed();
        String text = list.isEmpty() ? "Não há dados de funcionários que fecharam chamados no período."
                : "Os funcionários que mais fecharam chamados no período foram: "
                + list.stream().map(e -> String.format("%s com %d chamados", e.employeeName(), e.count())).collect(Collectors.joining(", "))
                + ", conforme os dados do sistema.";
        document.add(new Paragraph(text).setFontSize(11));
        document.add(new Paragraph("\n"));
    }

    private void addSection4TempoMedioPorGrupoPdf(Document document, ReportData reportData) {
        addSectionTitlePdf(document, "4. Tempo médio de resposta por grupo");
        List<GroupAvgTime> list = reportData.avgResponseTimeByGroup();
        String text = list.isEmpty() ? "Não há dados de tempo médio de resposta por grupo no período."
                : "Por grupo de WhatsApp, o tempo médio de resposta no período foi: "
                + list.stream().map(g -> String.format("Grupo %s, tempo médio de %s", g.groupName(), g.avgTimeFormatted())).collect(Collectors.joining(". "))
                + ". E assim para cada grupo monitorado.";
        document.add(new Paragraph(text).setFontSize(11));
        document.add(new Paragraph("\n"));
    }

    private void addSection5AlertasPorGrupoPdf(Document document, ReportData reportData) {
        addSectionTitlePdf(document, "5. Alertas que mais saíram por grupo e quantidade");
        List<GroupAlertCount> list = reportData.alertsByGroup();
        if (list.isEmpty()) {
            document.add(new Paragraph("Não há dados de alertas por grupo no período.").setFontSize(11));
        } else {
            for (GroupAlertCount g : list) {
                String alerts = g.alerts().stream().map(a -> String.format("%s com %d ocorrências", a.alertName(), a.count())).collect(Collectors.joining(", "));
                document.add(new Paragraph(String.format("No grupo %s, os termos de alerta que mais dispararam foram: %s.", g.groupName(), alerts)).setFontSize(11));
            }
            document.add(new Paragraph("O mesmo formato se repete para cada grupo, com o nome do grupo, o nome de cada alerta e a quantidade de cada um.").setFontSize(11));
        }
        document.add(new Paragraph("\n"));
    }

    private void addSection6ResumoPorStatusPdf(Document document, ReportData reportData) {
        addSectionTitlePdf(document, "6. Resumo por status de chamado");
        String text = String.format("Chamados abertos no período: %d. Chamados fechados com solução: %d. Chamados fechados sem solução: %d.",
                reportData.openedInPeriod(), reportData.closedWithSolution(), reportData.closedWithoutSolution());
        document.add(new Paragraph(text).setFontSize(11));
        document.add(new Paragraph("\n"));
    }

    private void addSection7ResumoPorTermoAlertaPdf(Document document, ReportData reportData) {
        addSectionTitlePdf(document, "7. Resumo por termo de alerta (geral)");
        List<AlertCount> list = reportData.topAlertTermsGeneral();
        String text = list.isEmpty() ? "Não há dados de termos de alerta no período."
                : "Considerando todos os grupos, os termos de alerta que mais geraram chamados no período foram: "
                + list.stream().map(a -> String.format("%s com %d chamados", a.alertName(), a.count())).collect(Collectors.joining(", "))
                + ", e assim por diante.";
        document.add(new Paragraph(text).setFontSize(11));
    }

    private void addSectionTitlePdf(Document document, String title) {
        document.add(new Paragraph(title).setFontSize(14).setBold());
        document.add(new Paragraph("\n"));
    }

    // --- DOCX ---
    private void addHeaderDocx(XWPFDocument doc, ReportData reportData) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText("RELATÓRIO TRANSRV");
        run.setBold(true);
        run.setFontSize(22);
        addEmptyParagraph(doc);
        addParagraphCenter(doc, String.format("Período: %s a %s.", reportData.startDate().format(DATE_TIME_FORMATTER), reportData.endDate().format(DATE_TIME_FORMATTER)), 12);
        addParagraphCenter(doc, "Gerado em: " + reportData.generatedAt().format(DATE_TIME_FULL) + ".", 12);
        addEmptyParagraph(doc);
        addEmptyParagraph(doc);
    }

    private void addSection1ResumoGeralDocx(XWPFDocument doc, ReportData reportData) {
        addSectionTitleDocx(doc, "1. Resumo geral do período");
        String text = String.format(
                "No período foram abertos %d chamados. Foram fechados %d chamados com solução e %d chamados fechados sem solução. O total de mensagens trocadas no período foi de %d.",
                reportData.openedInPeriod(), reportData.closedWithSolution(), reportData.closedWithoutSolution(), reportData.totalMessages());
        addParagraph(doc, text, 11);
        addEmptyParagraph(doc);
    }

    private void addSection2FuncionariosMaisAbrirDocx(XWPFDocument doc, ReportData reportData) {
        addSectionTitleDocx(doc, "2. Funcionários que mais abriram chamados");
        List<EmployeeCount> list = reportData.topEmployeesOpened();
        String text = list.isEmpty() ? "Não há dados de funcionários que abriram chamados no período."
                : "Os funcionários que mais abriram chamados no período foram: "
                + list.stream().map(e -> String.format("%s com %d chamados", e.employeeName(), e.count())).collect(Collectors.joining(", "))
                + ", conforme os dados do sistema.";
        addParagraph(doc, text, 11);
        addEmptyParagraph(doc);
    }

    private void addSection3FuncionariosMaisFecharDocx(XWPFDocument doc, ReportData reportData) {
        addSectionTitleDocx(doc, "3. Funcionários que mais fecharam chamados");
        List<EmployeeCount> list = reportData.topEmployeesClosed();
        String text = list.isEmpty() ? "Não há dados de funcionários que fecharam chamados no período."
                : "Os funcionários que mais fecharam chamados no período foram: "
                + list.stream().map(e -> String.format("%s com %d chamados", e.employeeName(), e.count())).collect(Collectors.joining(", "))
                + ", conforme os dados do sistema.";
        addParagraph(doc, text, 11);
        addEmptyParagraph(doc);
    }

    private void addSection4TempoMedioPorGrupoDocx(XWPFDocument doc, ReportData reportData) {
        addSectionTitleDocx(doc, "4. Tempo médio de resposta por grupo");
        List<GroupAvgTime> list = reportData.avgResponseTimeByGroup();
        String text = list.isEmpty() ? "Não há dados de tempo médio de resposta por grupo no período."
                : "Por grupo de WhatsApp, o tempo médio de resposta no período foi: "
                + list.stream().map(g -> String.format("Grupo %s, tempo médio de %s", g.groupName(), g.avgTimeFormatted())).collect(Collectors.joining(". "))
                + ". E assim para cada grupo monitorado.";
        addParagraph(doc, text, 11);
        addEmptyParagraph(doc);
    }

    private void addSection5AlertasPorGrupoDocx(XWPFDocument doc, ReportData reportData) {
        addSectionTitleDocx(doc, "5. Alertas que mais saíram por grupo e quantidade");
        List<GroupAlertCount> list = reportData.alertsByGroup();
        if (list.isEmpty()) {
            addParagraph(doc, "Não há dados de alertas por grupo no período.", 11);
        } else {
            for (GroupAlertCount g : list) {
                String alerts = g.alerts().stream().map(a -> String.format("%s com %d ocorrências", a.alertName(), a.count())).collect(Collectors.joining(", "));
                addParagraph(doc, String.format("No grupo %s, os termos de alerta que mais dispararam foram: %s.", g.groupName(), alerts), 11);
            }
            addParagraph(doc, "O mesmo formato se repete para cada grupo, com o nome do grupo, o nome de cada alerta e a quantidade de cada um.", 11);
        }
        addEmptyParagraph(doc);
    }

    private void addSection6ResumoPorStatusDocx(XWPFDocument doc, ReportData reportData) {
        addSectionTitleDocx(doc, "6. Resumo por status de chamado");
        String text = String.format("Chamados abertos no período: %d. Chamados fechados com solução: %d. Chamados fechados sem solução: %d.",
                reportData.openedInPeriod(), reportData.closedWithSolution(), reportData.closedWithoutSolution());
        addParagraph(doc, text, 11);
        addEmptyParagraph(doc);
    }

    private void addSection7ResumoPorTermoAlertaDocx(XWPFDocument doc, ReportData reportData) {
        addSectionTitleDocx(doc, "7. Resumo por termo de alerta (geral)");
        List<AlertCount> list = reportData.topAlertTermsGeneral();
        String text = list.isEmpty() ? "Não há dados de termos de alerta no período."
                : "Considerando todos os grupos, os termos de alerta que mais geraram chamados no período foram: "
                + list.stream().map(a -> String.format("%s com %d chamados", a.alertName(), a.count())).collect(Collectors.joining(", "))
                + ", e assim por diante.";
        addParagraph(doc, text, 11);
    }

    private void addSectionTitleDocx(XWPFDocument doc, String title) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(14);
        addEmptyParagraph(doc);
    }

    private void addParagraph(XWPFDocument doc, String text, int fontSize) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setFontSize(fontSize);
    }

    private void addParagraphCenter(XWPFDocument doc, String text, int fontSize) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setFontSize(fontSize);
    }

    private void addEmptyParagraph(XWPFDocument doc) {
        doc.createParagraph();
    }
}
