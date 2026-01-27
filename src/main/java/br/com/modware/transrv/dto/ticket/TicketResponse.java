
package br.com.modware.transrv.dto.ticket;

import br.com.modware.transrv.model.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resposta com dados completos de um ticket")
public record TicketResponse(
    Long id,
    Integer currentEscalationLevel,
    String groupName,
    String groupEvolutionId,
    String alertTermCode,
    String alertTermName,
    String alertTermDescription,
    Ticket.Status status,
    LocalDateTime createdAt,
    LocalDateTime closedAt,
    String openingMessageContent,
    String contactName,
    String contactPhone,
    String employeeName,
    Boolean openedIncorrectly
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getCurrentEscalationLevel(),
            ticket.getWaGroup() != null ? ticket.getWaGroup().getGroupName() : null,
            ticket.getWaGroup() != null ? ticket.getWaGroup().getEvolutionGroupId() : null,
            ticket.getAlertTerm() != null ? ticket.getAlertTerm().getCode() : null,
            ticket.getAlertTerm() != null ? ticket.getAlertTerm().getName() : null,
            ticket.getAlertTerm() != null ? ticket.getAlertTerm().getDescription() : null,
            ticket.getStatus(),
            ticket.getCreatedAt(),
            ticket.getClosedAt(),
            ticket.getMessageResponsibleForOpeningTheCall() != null 
                ? ticket.getMessageResponsibleForOpeningTheCall().getMessageContent() : null,
            ticket.getContactResponsibleForOpeningTheCall() != null 
                ? ticket.getContactResponsibleForOpeningTheCall().getName() : null,
            ticket.getContactResponsibleForOpeningTheCall() != null 
                ? ticket.getContactResponsibleForOpeningTheCall().getPhoneNumber() : null,
            ticket.getEmployeeResponsibleForOpeningTheCall() != null 
                ? ticket.getEmployeeResponsibleForOpeningTheCall().getName() : null,
            ticket.getOpenedIncorrectly()
        );
    }
}