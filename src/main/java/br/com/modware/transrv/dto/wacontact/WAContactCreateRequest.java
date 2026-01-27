package br.com.modware.transrv.dto.wacontact;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de um novo contato WhatsApp")
public record WAContactCreateRequest(
    @Schema(description = "Nome do contato", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    @Schema(description = "Número de telefone do contato", example = "5511999999999", requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber
) {}