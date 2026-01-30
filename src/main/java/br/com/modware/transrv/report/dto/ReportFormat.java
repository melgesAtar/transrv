package br.com.modware.transrv.report.dto;

/**
 * Formato do documento do relatório aceito pelo usuário.
 */
public enum ReportFormat {
    PDF,
    DOCX;

    public static ReportFormat fromString(String value) {
        if (value == null || value.isBlank()) {
            return PDF;
        }
        return switch (value.toUpperCase()) {
            case "DOCX" -> DOCX;
            default -> PDF;
        };
    }
}
