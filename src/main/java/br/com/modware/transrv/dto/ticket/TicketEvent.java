package br.com.modware.transrv.dto.ticket;

import br.com.modware.transrv.model.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Evento de ticket enviado via WebSocket quando há mudanças em tickets")
public record TicketEvent(
    @Schema(description = "Tipo do evento", example = "TICKET_OPENED", 
            allowableValues = {"TICKET_OPENED", "TICKET_CLOSED", "TICKET_UPDATED", "TICKET_ESCALATED", "TICKET_MARKED_INCORRECT"})
    EventType type,
    
    @Schema(description = "Dados completos do ticket")
    TicketResponse ticket,
    
    @Schema(description = "Timestamp do evento", example = "2024-01-26T22:30:00")
    LocalDateTime timestamp
) {
    @Schema(description = "Tipos de eventos de ticket")
    public enum EventType {
        @Schema(description = "Novo ticket foi aberto")
        TICKET_OPENED,
        
        @Schema(description = "Ticket foi fechado")
        TICKET_CLOSED,
        
        @Schema(description = "Ticket foi atualizado (mudança de nível ou outros campos)")
        TICKET_UPDATED,
        
        @Schema(description = "Ticket foi escalado para um nível superior")
        TICKET_ESCALATED,
        
        @Schema(description = "Ticket foi marcado como aberto incorretamente")
        TICKET_MARKED_INCORRECT
    }
    
    public static TicketEvent opened(Ticket ticket) {
        return new TicketEvent(
            EventType.TICKET_OPENED,
            TicketResponse.from(ticket),
            LocalDateTime.now()
        );
    }
    
    public static TicketEvent closed(Ticket ticket) {
        return new TicketEvent(
            EventType.TICKET_CLOSED,
            TicketResponse.from(ticket),
            LocalDateTime.now()
        );
    }
    
    public static TicketEvent updated(Ticket ticket) {
        return new TicketEvent(
            EventType.TICKET_UPDATED,
            TicketResponse.from(ticket),
            LocalDateTime.now()
        );
    }
    
    public static TicketEvent escalated(Ticket ticket) {
        return new TicketEvent(
            EventType.TICKET_ESCALATED,
            TicketResponse.from(ticket),
            LocalDateTime.now()
        );
    }
    
    public static TicketEvent markedIncorrect(Ticket ticket) {
        return new TicketEvent(
            EventType.TICKET_MARKED_INCORRECT,
            TicketResponse.from(ticket),
            LocalDateTime.now()
        );
    }
}
