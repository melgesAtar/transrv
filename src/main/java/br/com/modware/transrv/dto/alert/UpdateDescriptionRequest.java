package br.com.modware.transrv.dto.alert;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualizar a descrição de um termo de alerta")
public record UpdateDescriptionRequest(
    @Schema(description = "Nova descrição do termo de alerta", example = "Nova descrição do alerta", requiredMode = Schema.RequiredMode.REQUIRED)
    String description
) {}