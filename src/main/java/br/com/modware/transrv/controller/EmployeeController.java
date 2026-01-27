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
import br.com.modware.transrv.service.EmployeeAlertTermService;
import java.util.List;
import br.com.modware.transrv.dto.employee.EmployeeAlertTermResponse;
import br.com.modware.transrv.dto.employee.LinkAlertTermRequest;
import br.com.modware.transrv.dto.employee.UnlinkAlertTermRequest;
import br.com.modware.transrv.service.EmployeeWAContactService;
import br.com.modware.transrv.dto.employee.EmployeeWAContactResponse;
import br.com.modware.transrv.dto.employee.LinkWAContactRequest;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Funcionários", description = "Endpoints para consulta de funcionários")
@SecurityRequirement(name = "JWT")
public class EmployeeController {
    
    private final EmployeeQueryService employeeQueryService;
    private final EmployeeService employeeService;
    private final EmployeeAlertTermService employeeAlertTermService;
    private final EmployeeWAContactService employeeWAContactService;
    public EmployeeController(EmployeeQueryService employeeQueryService, EmployeeService employeeService, EmployeeAlertTermService employeeAlertTermService, EmployeeWAContactService employeeWAContactService) {
        this.employeeService = employeeService;
        this.employeeQueryService = employeeQueryService;
        this.employeeAlertTermService = employeeAlertTermService;
        this.employeeWAContactService = employeeWAContactService;
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

    
    @Operation(
        summary = "Listar alertas vinculados ao funcionário",
        description = "Retorna uma lista de todos os alertas vinculados a um funcionário, incluindo o nível de prioridade de cada vínculo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de alertas vinculados retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/{id}/alert-terms")
    public ResponseEntity<List<EmployeeAlertTermResponse>> getEmployeeAlertTerms(
            @Parameter(description = "ID do funcionário", required = true, example = "1")
            @PathVariable Long id) {
        
        var employeeAlertTerms = employeeAlertTermService.findByEmployee(id);
        List<EmployeeAlertTermResponse> response = employeeAlertTerms.stream()
                .map(EmployeeAlertTermResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Vincular alerta ao funcionário",
        description = "Vincula um termo de alerta a um funcionário com um nível de prioridade específico. Não permite vínculos duplicados (mesmo funcionário + alerta + nível de prioridade). Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alerta vinculado com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeeAlertTermResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (vínculo duplicado, nível de prioridade inválido, funcionário ou alerta não encontrado)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Funcionário ou alerta não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/alert-terms")
    public ResponseEntity<EmployeeAlertTermResponse> linkAlertTerm(
            @Parameter(description = "ID do funcionário", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dados do vínculo (alertTermId e priorityLevel)")
            @RequestBody LinkAlertTermRequest request) {
        
        var employeeAlertTerm = employeeAlertTermService.linkAlertTerm(
                id, 
                request.alertTermId(), 
                request.priorityLevel()
        );
        EmployeeAlertTermResponse response = EmployeeAlertTermResponse.from(employeeAlertTerm);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desvincular alerta do funcionário",
        description = "Remove o vínculo entre um termo de alerta e um funcionário para um nível de prioridade específico. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alerta desvinculado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (nível de prioridade inválido)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Funcionário, alerta ou vínculo não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/alert-terms")
    public ResponseEntity<Void> unlinkAlertTerm(
            @Parameter(description = "ID do funcionário", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dados do vínculo a ser removido (alertTermId e priorityLevel)")
            @RequestBody UnlinkAlertTermRequest request) {
        
        employeeAlertTermService.unlinkAlertTerm(
                id, 
                request.alertTermId(), 
                request.priorityLevel()
        );
        return ResponseEntity.ok().build();
    }


    @Operation(
        summary = "Listar contatos WhatsApp vinculados ao funcionário",
        description = "Retorna uma lista de todos os contatos WhatsApp vinculados a um funcionário"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contatos vinculados retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Funcionário não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping("/{id}/wa-contacts")
    public ResponseEntity<List<EmployeeWAContactResponse>> getEmployeeWAContacts(
            @Parameter(description = "ID do funcionário", required = true, example = "1")
            @PathVariable Long id) {
        
        var employeeWAContacts = employeeWAContactService.findByEmployee(id);
        List<EmployeeWAContactResponse> response = employeeWAContacts.stream()
                .map(EmployeeWAContactResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Vincular contato WhatsApp ao funcionário",
        description = "Vincula um contato WhatsApp a um funcionário. Não permite vínculos duplicados. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contato vinculado com sucesso",
            content = @Content(schema = @Schema(implementation = EmployeeWAContactResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (vínculo duplicado, funcionário ou contato não encontrado)"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Funcionário ou contato não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/wa-contacts")
    public ResponseEntity<EmployeeWAContactResponse> linkWAContact(
            @Parameter(description = "ID do funcionário", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dados do vínculo (waContactId)")
            @RequestBody LinkWAContactRequest request) {
        
        var employeeWAContact = employeeWAContactService.linkWAContact(id, request.waContactId());
        EmployeeWAContactResponse response = EmployeeWAContactResponse.from(employeeWAContact);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Desvincular contato WhatsApp do funcionário",
        description = "Remove o vínculo entre um contato WhatsApp e um funcionário. Apenas usuários com role ADMIN podem executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contato desvinculado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (apenas ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Funcionário, contato ou vínculo não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/wa-contacts/{waContactId}")
    public ResponseEntity<Void> unlinkWAContact(
            @Parameter(description = "ID do funcionário", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID do contato WhatsApp", required = true, example = "1")
            @PathVariable Long waContactId) {
        
        employeeWAContactService.unlinkWAContact(id, waContactId);
        return ResponseEntity.ok().build();
    }

}
