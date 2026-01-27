package br.com.modware.transrv.dto.alert;

import br.com.modware.transrv.model.AlertTerm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resposta com dados de um termo de alerta")
public record AlertTermResponse(
    @Schema(description = "ID do alerta", example = "1")
    Long id,
    @Schema(description = "Código único do alerta", example = "RISCO_ETA_ORIGEM")
    String code,
    @Schema(description = "Nome do alerta", example = "Risco ETA Origem")
    String name,
    @Schema(description = "Descrição do alerta", example = "Alerta para risco de ETA na origem")
    String description,
    @Schema(description = "Status do alerta (ACTIVE ou INACTIVE)", example = "ACTIVE")
    AlertTerm.Status status,
    @Schema(description = "Data de criação", example = "2024-01-27T10:00:00")
    LocalDateTime createdAt,
    @Schema(description = "Data de última atualização", example = "2024-01-27T10:00:00")
    LocalDateTime updatedAt
) {
    public static AlertTermResponse from(AlertTerm alertTerm) {
        return new AlertTermResponse(
            alertTerm.getId(),
            alertTerm.getCode(),
            alertTerm.getName(),
            alertTerm.getDescription(),
            alertTerm.getStatus(),
            alertTerm.getCreatedAt(),
            alertTerm.getUpdatedAt()
        );
    }
}
