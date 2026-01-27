package br.com.modware.transrv.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualização de um funcionário")
public record EmployeeUpdateRequest(
    @Schema(description = "Nome do funcionário", example = "João Silva")
    String name,
    @Schema(description = "ID do departamento (pode ser null para remover o departamento)", example = "1")
    Long departmentId,
    @Schema(description = "Indica se o funcionário está ativo", example = "true")
    Boolean isActive
) {}