package br.com.modware.transrv.dto.alert;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de um novo termo de alerta")
public record AlertTermCreateRequest(
    @Schema(description = "Nome do termo de alerta", example = "Risco ETA Origem", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    @Schema(description = "Descrição do termo de alerta", example = "Alerta para risco de ETA na origem", requiredMode = Schema.RequiredMode.REQUIRED)
    String description
) {}