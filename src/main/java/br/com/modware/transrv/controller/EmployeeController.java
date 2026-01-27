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
import br.com.modware.transrv.dto.employee.EmployeeCreateRequest;
import br.com.modware.transrv.dto.employee.EmployeeUpdateRequest;
import br.com.modware.transrv.dto.employee.EmployeeResponse;
import br.com.modware.transrv.service.EmployeeService;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Funcionários", description = "Endpoints para consulta de funcionários")
@SecurityRequirement(name = "JWT")
public class EmployeeController {
    
    private final EmployeeQueryService employeeQueryService;
    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeQueryService employeeQueryService, EmployeeService employeeService) {
        this.employeeService = employeeService;
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

    @Operation(
        summary = "Criar novo funcionário",
        description = "Cria um novo funcionário no sistema. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Funcionário criado com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (nome vazio ou departamento não encontrado)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Parameter(description = "Dados do novo funcionário")
            @RequestBody EmployeeCreateRequest request) {
        
        var employee = employeeService.createEmployee(request);
        EmployeeResponse response = EmployeeResponse.from(employee);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Atualizar funcionário",
        description = "Atualiza os dados de um funcionário existente. Todos os campos são opcionais - apenas os campos enviados serão atualizados. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (nome vazio ou departamento não encontrado)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @Parameter(description = "ID do funcionário a ser atualizado", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dados do funcionário a serem atualizados")
            @RequestBody EmployeeUpdateRequest request) {
        
        var employee = employeeService.updateEmployee(id, request);
        EmployeeResponse response = EmployeeResponse.from(employee);
        return ResponseEntity.ok(response);
    }

}
