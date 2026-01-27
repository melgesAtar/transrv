package br.com.modware.transrv.dto.wacontact;

import br.com.modware.transrv.model.WAContact;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um contato WhatsApp")
public record WAContactResponse(
    @Schema(description = "ID do contato", example = "1")
    Long id,
    @Schema(description = "Nome do contato", example = "João Silva")
    String name,
    @Schema(description = "Número de telefone do contato", example = "5511999999999")
    String phoneNumber
) {
    public static WAContactResponse from(WAContact waContact) {
        return new WAContactResponse(
            waContact.getId(),
            waContact.getName(),
            waContact.getPhoneNumber()
        );
    }
}