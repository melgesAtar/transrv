package br.com.modware.transrv.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualizar o agente vinculado a um grupo")
public record UpdateAgentRequest(
    @Schema(description = "ID do agente a ser vinculado ao grupo", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long agentId
) {}
