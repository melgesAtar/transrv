package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.agent.AgentFilterRequest;
import br.com.modware.transrv.dto.agent.AgentPageResponse;
import br.com.modware.transrv.service.AgentQueryService;
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

@RestController
@RequestMapping("/api/agents")
@Tag(name = "Agentes", description = "Endpoints para consulta de agentes")
@SecurityRequirement(name = "JWT")
public class AgentController {
    
    private final AgentQueryService agentQueryService;

    public AgentController(AgentQueryService agentQueryService) {
        this.agentQueryService = agentQueryService;
    }

    @Operation(
        summary = "Listar agentes com filtros",
        description = "Retorna uma lista paginada de agentes com filtro opcional por nome. Busca parcial e case-insensitive."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de agentes retornada com sucesso",
            content = @Content(schema = @Schema(implementation = AgentPageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<AgentPageResponse> getAgents(
            @Parameter(description = "Nome do agente para filtrar (busca parcial, case-insensitive)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo para ordenação (id, name)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        AgentFilterRequest filter = new AgentFilterRequest(
            name,
            page,
            size,
            sortBy,
            sortDirection
        );
        
        AgentPageResponse response = agentQueryService.findAgents(filter);
        return ResponseEntity.ok(response);
    }
}
