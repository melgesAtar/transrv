package br.com.modware.transrv.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de um novo funcionário")
public record EmployeeCreateRequest(
    @Schema(description = "Nome do funcionário", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    @Schema(description = "ID do departamento (opcional)", example = "1")
    Long departmentId  
) {}