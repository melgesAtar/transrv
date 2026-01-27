package br.com.modware.transrv.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para desvincular um alerta de um funcionário")
public record UnlinkAlertTermRequest(
    @Schema(description = "ID do termo de alerta", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long alertTermId,
    @Schema(description = "Nível de prioridade (1, 2 ou 3)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer priorityLevel
) {}