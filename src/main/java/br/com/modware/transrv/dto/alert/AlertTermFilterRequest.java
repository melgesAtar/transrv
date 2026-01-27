package br.com.modware.transrv.dto.alert;

import br.com.modware.transrv.model.AlertTerm;

public record AlertTermFilterRequest(
    String code,                    // Filtro por código do alerta (busca parcial, case-insensitive)
    String name,                   // Filtro por nome do alerta (busca parcial, case-insensitive)
    AlertTerm.Status status,       // Filtro por status (ACTIVE, INACTIVE)
    Integer page,                  // Número da página (base 0)
    Integer size,                  // Tamanho da página
    String sortBy,                 // Campo para ordenação
    String sortDirection           // Direção da ordenação (ASC ou DESC)
) {
    public AlertTermFilterRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // Limite máximo
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "ASC";
    }
}
