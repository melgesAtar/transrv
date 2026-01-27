package br.com.modware.transrv.dto.employee;

import br.com.modware.transrv.dto.alert.AlertTermResponse;
import br.com.modware.transrv.model.EmployeeAlertTerm;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um alerta vinculado ao funcionário")
public record EmployeeAlertTermResponse(
    @Schema(description = "ID do vínculo", example = "1")
    Long id,
    @Schema(description = "Dados do termo de alerta")
    AlertTermResponse alertTerm,
    @Schema(description = "Nível de prioridade (1, 2 ou 3)", example = "1")
    Integer priorityLevel
) {
    public static EmployeeAlertTermResponse from(EmployeeAlertTerm employeeAlertTerm) {
        return new EmployeeAlertTermResponse(
            employeeAlertTerm.getId(),
            AlertTermResponse.from(employeeAlertTerm.getAlertTerm()),
            employeeAlertTerm.getPriorityLevel()
        );
    }
}