package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.alert.AlertTermFilterRequest;
import br.com.modware.transrv.dto.alert.AlertTermPageResponse;
import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.service.AlertTermQueryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.modware.transrv.dto.alert.UpdateDescriptionRequest;
import br.com.modware.transrv.dto.alert.AlertTermResponse;
import br.com.modware.transrv.service.AlertTermsService;


@RestController
@RequestMapping("/api/alert-terms")
@Tag(name = "Termos de Alerta", description = "Endpoints para consulta de termos de alerta")
@SecurityRequirement(name = "JWT")
public class AlertTermController {
    
    private final AlertTermQueryService alertTermQueryService;
    private final AlertTermsService alertTermsService;
    public AlertTermController(AlertTermQueryService alertTermQueryService, AlertTermsService alertTermsService) {
        this.alertTermQueryService = alertTermQueryService;
        this.alertTermsService = alertTermsService;
    }

    @Operation(
        summary = "Listar termos de alerta com filtros",
        description = "Retorna uma lista paginada de termos de alerta com filtros opcionais por código, nome e status (ACTIVE/INACTIVE)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de termos de alerta retornada com sucesso",
            content = @Content(schema = @Schema(implementation = AlertTermPageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<AlertTermPageResponse> getAlertTerms(
            @Parameter(description = "Código do alerta para filtrar (busca parcial, case-insensitive)")
            @RequestParam(required = false) String code,
            @Parameter(description = "Nome do alerta para filtrar (busca parcial, case-insensitive)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Status do alerta (ACTIVE ou INACTIVE)")
            @RequestParam(required = false) AlertTerm.Status status,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo para ordenação (id, code, name, status)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        AlertTermFilterRequest filter = new AlertTermFilterRequest(
            code,
            name,
            status,
            page,
            size,
            sortBy,
            sortDirection
        );
        
        AlertTermPageResponse response = alertTermQueryService.findAlertTerms(filter);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar descrição de um termo de alerta",
        description = "Atualiza a descrição de um termo de alerta. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Descrição atualizada com sucesso",
            content = @Content(schema = @Schema(implementation = AlertTermResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (descrição vazia ou muito longa)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Termo de alerta não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/description")
    public ResponseEntity<AlertTermResponse> updateDescription(
            @Parameter(description = "ID do termo de alerta a ser atualizado", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nova descrição do termo de alerta")
            @RequestBody UpdateDescriptionRequest request) {
        
        AlertTerm alertTerm = alertTermsService.updateDescription(id, request.description());
        AlertTermResponse response = AlertTermResponse.from(alertTerm);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desabilitar um termo de alerta",
        description = "Desabilita um termo de alerta, alterando seu status para INACTIVE. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Termo de alerta desabilitado com sucesso",
            content = @Content(schema = @Schema(implementation = AlertTermResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Termo de alerta não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/disable")
    public ResponseEntity<AlertTermResponse> disable(
            @Parameter(description = "ID do termo de alerta a ser desabilitado", required = true, example = "1")
            @PathVariable Long id) {
        
        AlertTerm alertTerm = alertTermsService.disable(id);
        AlertTermResponse response = AlertTermResponse.from(alertTerm);
        return ResponseEntity.ok(response);
    }
    
}
