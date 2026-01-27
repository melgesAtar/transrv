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

@RestController
@RequestMapping("/api/alert-terms")
@Tag(name = "Termos de Alerta", description = "Endpoints para consulta de termos de alerta")
@SecurityRequirement(name = "JWT")
public class AlertTermController {
    
    private final AlertTermQueryService alertTermQueryService;

    public AlertTermController(AlertTermQueryService alertTermQueryService) {
        this.alertTermQueryService = alertTermQueryService;
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
}
