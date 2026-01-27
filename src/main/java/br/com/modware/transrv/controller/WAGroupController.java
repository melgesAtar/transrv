package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.group.UpdateAgentRequest;
import br.com.modware.transrv.dto.group.WAGroupFilterRequest;
import br.com.modware.transrv.dto.group.WAGroupPageResponse;
import br.com.modware.transrv.dto.group.WAGroupResponse;
import br.com.modware.transrv.service.WAGroupQueryService;
import br.com.modware.transrv.service.WAGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Grupos WhatsApp", description = "Endpoints para consulta e gerenciamento de grupos de WhatsApp")
@SecurityRequirement(name = "JWT")
public class WAGroupController {
    
    private final WAGroupQueryService waGroupQueryService;
    private final WAGroupService waGroupService;

    public WAGroupController(WAGroupQueryService waGroupQueryService, WAGroupService waGroupService) {
        this.waGroupQueryService = waGroupQueryService;
        this.waGroupService = waGroupService;
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

    @Operation(
        summary = "Atualizar agente vinculado ao grupo",
        description = "Vincula um novo agente ao grupo de WhatsApp. Apenas usuários ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agente atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = WAGroupResponse.class))),
        @ApiResponse(responseCode = "400", description = "Grupo ou agente não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/agent")
    public ResponseEntity<WAGroupResponse> updateAgent(
            @Parameter(description = "ID do grupo", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateAgentRequest request) {
        
        var group = waGroupService.updateAgent(id, request.agentId());
        return ResponseEntity.ok(WAGroupResponse.from(group));
    }

    @Operation(
        summary = "Desabilitar agente no grupo",
        description = "Remove o agente vinculado ao grupo de WhatsApp, desabilitando-o. Apenas usuários ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Agente desabilitado com sucesso",
            content = @Content(schema = @Schema(implementation = WAGroupResponse.class))),
        @ApiResponse(responseCode = "400", description = "Grupo não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/agent")
    public ResponseEntity<WAGroupResponse> removeAgent(
            @Parameter(description = "ID do grupo", required = true, example = "1")
            @PathVariable Long id) {
        
        var group = waGroupService.removeAgent(id);
        return ResponseEntity.ok(WAGroupResponse.from(group));
    }
}
