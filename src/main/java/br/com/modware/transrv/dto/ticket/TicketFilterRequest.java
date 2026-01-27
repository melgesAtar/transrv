package br.com.modware.transrv.dto.ticket;

import java.time.LocalDateTime;
import java.util.List;

import br.com.modware.transrv.model.Ticket;

public record TicketFilterRequest(
    Long groupId,                         
    List<Integer> escalationLevels,       
    List<Long> alertTermIds,               
    Ticket.Status status,                  
    LocalDateTime startDate,               
    LocalDateTime endDate,                 
    Integer page,                         
    Integer size,                        
    String sortBy,                         
    String sortDirection                   
) {
    public TicketFilterRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; 
        if (sortBy == null || sortBy.isBlank()) sortBy = "createdAt";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "DESC";
    }
}