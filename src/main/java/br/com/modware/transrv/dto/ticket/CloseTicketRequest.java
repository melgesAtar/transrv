package br.com.modware.transrv.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para fechar um ticket com opção de vincular funcionário responsável")
public record CloseTicketRequest(
    @Schema(description = "ID do funcionário responsável pelo fechamento do ticket (opcional)", example = "1")
    Long employeeId
) {}
