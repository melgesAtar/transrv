package br.com.modware.transrv.dto.group;

import java.util.List;

public record WAGroupPageResponse(
    List<WAGroupResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
