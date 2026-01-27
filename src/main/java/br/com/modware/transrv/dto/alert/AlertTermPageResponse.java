package br.com.modware.transrv.dto.alert;

import java.util.List;

public record AlertTermPageResponse(
    List<AlertTermResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
