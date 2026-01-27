package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.group.WAGroupFilterRequest;
import br.com.modware.transrv.dto.group.WAGroupPageResponse;
import br.com.modware.transrv.service.WAGroupQueryService;
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
@RequestMapping("/api/groups")
@Tag(name = "Grupos WhatsApp", description = "Endpoints para consulta de grupos de WhatsApp")
@SecurityRequirement(name = "JWT")
public class WAGroupController {
    
    private final WAGroupQueryService waGroupQueryService;

    public WAGroupController(WAGroupQueryService waGroupQueryService) {
        this.waGroupQueryService = waGroupQueryService;
    }

    @Operation(
        summary = "Listar grupos de WhatsApp com filtros",
        description = "Retorna uma lista paginada de grupos de WhatsApp com filtros opcionais por nome, ID do Evolution, agente e status de monitoramento"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de grupos retornada com sucesso",
            content = @Content(schema = @Schema(implementation = WAGroupPageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<WAGroupPageResponse> getGroups(
            @Parameter(description = "Nome do grupo para filtrar (busca parcial, case-insensitive)")
            @RequestParam(required = false) String groupName,
            @Parameter(description = "ID do grupo no Evolution API para filtrar (busca exata)")
            @RequestParam(required = false) String evolutionGroupId,
            @Parameter(description = "ID do agente associado para filtrar")
            @RequestParam(required = false) Long agentId,
            @Parameter(description = "Filtrar por status de monitoramento (true/false)")
            @RequestParam(required = false) Boolean isMonitored,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo para ordenação (id, groupName, evolutionGroupId)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        WAGroupFilterRequest filter = new WAGroupFilterRequest(
            groupName,
            evolutionGroupId,
            agentId,
            isMonitored,
            page,
            size,
            sortBy,
            sortDirection
        );
        
        WAGroupPageResponse response = waGroupQueryService.findGroups(filter);
        return ResponseEntity.ok(response);
    }
}
