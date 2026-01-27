package br.com.modware.transrv.dto.wacontact;

import java.util.List;

public record WAContactPageResponse(
    List<WAContactResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}