package br.com.modware.transrv.controller;

import br.com.modware.transrv.dto.employee.EmployeeFilterRequest;
import br.com.modware.transrv.dto.employee.EmployeePageResponse;
import br.com.modware.transrv.service.EmployeeQueryService;
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
@RequestMapping("/api/employees")
@Tag(name = "Funcionários", description = "Endpoints para consulta de funcionários")
@SecurityRequirement(name = "JWT")
public class EmployeeController {
    
    private final EmployeeQueryService employeeQueryService;

    public EmployeeController(EmployeeQueryService employeeQueryService) {
        this.employeeQueryService = employeeQueryService;
    }

    @Operation(
        summary = "Listar funcionários com filtros",
        description = "Retorna uma lista paginada de funcionários com filtros opcionais por nome, departamento e status ativo/inativo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de funcionários retornada com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeePageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<EmployeePageResponse> getEmployees(
            @Parameter(description = "Nome do funcionário para filtrar (busca parcial, case-insensitive)")
            @RequestParam(required = false) String name,
            @Parameter(description = "ID do departamento para filtrar")
            @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Filtrar por status ativo/inativo (true/false)")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo para ordenação (id, name)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        EmployeeFilterRequest filter = new EmployeeFilterRequest(
            name,
            departmentId,
            isActive,
            page,
            size,
            sortBy,
            sortDirection
        );
        
        EmployeePageResponse response = employeeQueryService.findEmployees(filter);
        return ResponseEntity.ok(response);
    }
}
