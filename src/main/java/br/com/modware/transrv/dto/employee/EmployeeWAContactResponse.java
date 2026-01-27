package br.com.modware.transrv.dto.employee;

import br.com.modware.transrv.dto.wacontact.WAContactResponse;
import br.com.modware.transrv.model.EmployeeWAContact;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um contato WhatsApp vinculado ao funcionário")
public record EmployeeWAContactResponse(
    @Schema(description = "ID do vínculo", example = "1")
    Long id,
    @Schema(description = "Dados do contato WhatsApp")
    WAContactResponse waContact
) {
    public static EmployeeWAContactResponse from(EmployeeWAContact employeeWAContact) {
        return new EmployeeWAContactResponse(
            employeeWAContact.getId(),
            WAContactResponse.from(employeeWAContact.getWaContact())
        );
    }
}