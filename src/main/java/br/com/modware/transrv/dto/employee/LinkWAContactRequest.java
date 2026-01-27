package br.com.modware.transrv.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para vincular um contato WhatsApp a um funcionário")
public record LinkWAContactRequest(
    @Schema(description = "ID do contato WhatsApp", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long waContactId
) {}