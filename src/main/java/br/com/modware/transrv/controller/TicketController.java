package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.ticket.TicketFilterRequest;
import br.com.modware.transrv.dto.ticket.TicketPageResponse;
import br.com.modware.transrv.dto.ticket.CloseTicketRequest;
import br.com.modware.transrv.dto.ticket.TicketResponse;
import br.com.modware.transrv.service.TicketQueryService;
import br.com.modware.transrv.service.TicketService;
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
    private final TicketService ticketService;

    public TicketController(TicketQueryService ticketQueryService, TicketService ticketService) {
        this.ticketQueryService = ticketQueryService;
        this.ticketService = ticketService;
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
            @Parameter(description = "Lista de IDs de grupos WhatsApp para filtrar (pode passar múltiplos valores)")
            @RequestParam(required = false) List<Long> groupIds,
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
            groupIds,
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

    @Operation(
        summary = "Buscar ticket por ID",
        description = "Retorna os detalhes completos de um ticket específico pelo seu ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = TicketResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ticket não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @Parameter(description = "ID do ticket a ser buscado", required = true, example = "1")
            @PathVariable Long id) {
        
        TicketResponse response = ticketQueryService.findTicketById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Fechar ticket",
        description = "Fecha um ticket aberto e permite vincular um funcionário responsável pelo fechamento. O employeeId é opcional."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket fechado com sucesso",
            content = @Content(schema = @Schema(implementation = TicketResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (ticket não está aberto ou funcionário não encontrado)"),
        @ApiResponse(responseCode = "404", description = "Ticket não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping("/{id}/close")
    public ResponseEntity<TicketResponse> closeTicket(
            @Parameter(description = "ID do ticket a ser fechado", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dados para fechamento do ticket (employeeId opcional). Pode ser enviado vazio {} ou omitido.")
            @RequestBody(required = false) CloseTicketRequest request) {
        
        Long employeeId = (request != null && request.employeeId() != null) ? request.employeeId() : null;
        Ticket ticket = ticketService.closeTicketWithEmployee(id, employeeId);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Marcar ticket como aberto incorretamente",
        description = "Marca um ticket como aberto incorretamente e o fecha automaticamente. Útil para feedback sobre tickets que foram abertos por engano."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket marcado como incorreto e fechado com sucesso",
            content = @Content(schema = @Schema(implementation = TicketResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (ticket não está aberto)"),
        @ApiResponse(responseCode = "404", description = "Ticket não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping("/{id}/incorrect")
    public ResponseEntity<TicketResponse> markAsIncorrect(
            @Parameter(description = "ID do ticket a ser marcado como incorreto", required = true, example = "1")
            @PathVariable Long id) {
        
        Ticket ticket = ticketService.closeAsIncorrect(id);
        TicketResponse response = TicketResponse.from(ticket);
        return ResponseEntity.ok(response);
    }
}