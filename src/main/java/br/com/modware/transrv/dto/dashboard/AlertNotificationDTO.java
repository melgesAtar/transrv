package br.com.modware.transrv.dto.dashboard;

import br.com.modware.transrv.model.Ticket;
import lombok.Data;

@Data
public class AlertNotificationDTO {
    private String type;
    private Long ticketId;
    private Integer escalationLevel;

    public AlertNotificationDTO(String type, Long ticketId, Integer escalationLevel) {
        this.type = type;
        this.ticketId = ticketId;
        this.escalationLevel = escalationLevel;
    }
}
