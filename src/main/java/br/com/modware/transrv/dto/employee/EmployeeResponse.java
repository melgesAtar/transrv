package br.com.modware.transrv.dto.employee;

import br.com.modware.transrv.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um funcionário")
public record EmployeeResponse(
    @Schema(description = "ID do funcionário", example = "1")
    Long id,
    @Schema(description = "Nome do funcionário", example = "João Silva")
    String name,
    @Schema(description = "ID do departamento (pode ser null)", example = "1")
    Long departmentId,
    @Schema(description = "Nome do departamento (pode ser null)", example = "Suporte")
    String departmentName,
    @Schema(description = "Indica se o funcionário está ativo", example = "true")
    Boolean isActive
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getName(),
            employee.getDepartment() != null ? employee.getDepartment().getId() : null,
            employee.getDepartment() != null ? employee.getDepartment().getName() : null,
            employee.isActive()
        );
    }
}
