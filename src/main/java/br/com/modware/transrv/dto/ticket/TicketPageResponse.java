package br.com.modware.transrv.dto.ticket;

import java.util.List;

public record TicketPageResponse(
    List<TicketResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}