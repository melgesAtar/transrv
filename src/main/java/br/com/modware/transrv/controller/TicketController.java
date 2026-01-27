package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.ticket.TicketFilterRequest;
import br.com.modware.transrv.dto.ticket.TicketPageResponse;
import br.com.modware.transrv.service.TicketQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import br.com.modware.transrv.model.Ticket;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Endpoints para consulta e gerenciamento de tickets/chamados")
@SecurityRequirement(name = "JWT")
public class TicketController {
    
    private final TicketQueryService ticketQueryService;

    public TicketController(TicketQueryService ticketQueryService) {
        this.ticketQueryService = ticketQueryService;
    }

    @Operation(
        summary = "Listar tickets com filtros",
        description = "Retorna uma lista paginada de tickets com filtros opcionais por grupo, nível de prioridade, tipo de alerta e período"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tickets retornada com sucesso",
            content = @Content(schema = @Schema(implementation = TicketPageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<TicketPageResponse> getTickets(
            @Parameter(description = "ID do grupo WhatsApp para filtrar")
            @RequestParam(required = false) Long groupId,
            @Parameter(description = "Lista de níveis de escalação (1, 2, 3) para filtrar")
            @RequestParam(required = false) List<Integer> escalationLevels,
            @Parameter(description = "Lista de IDs de tipos de alerta para filtrar")
            @RequestParam(required = false) List<Long> alertTermIds,
            @Parameter(description = "Status do ticket (OPEN, CLOSED, CLOSED_WITHOUT_SOLUTION)")
            @RequestParam(required = false) Ticket.Status status,
            @Parameter(description = "Data inicial do período (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) java.time.LocalDateTime startDate,
            @Parameter(description = "Data final do período (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) java.time.LocalDateTime endDate,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo para ordenação (createdAt, closedAt, status, etc.)", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        TicketFilterRequest filter = new TicketFilterRequest(
            groupId,
            escalationLevels,
            alertTermIds,
            status,
            startDate,
            endDate,
            page,
            size,
            sortBy,
            sortDirection
        );
        
        TicketPageResponse response = ticketQueryService.findTickets(filter);
        return ResponseEntity.ok(response);
    }
}