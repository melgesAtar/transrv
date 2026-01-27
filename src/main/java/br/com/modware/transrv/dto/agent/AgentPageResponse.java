package br.com.modware.transrv.dto.agent;

import java.util.List;

public record AgentPageResponse(
    List<AgentResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
