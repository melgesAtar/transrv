package br.com.modware.transrv.dto.group;

public record WAGroupFilterRequest(
    String groupName,               // Filtro por nome do grupo (busca parcial, case-insensitive)
    String evolutionGroupId,        // Filtro por ID do grupo no Evolution
    Long agentId,                   // Filtro por ID do agente associado
    Boolean isMonitored,            // Filtro por status de monitoramento
    Integer page,                   // Número da página (base 0)
    Integer size,                   // Tamanho da página
    String sortBy,                  // Campo para ordenação
    String sortDirection            // Direção da ordenação (ASC ou DESC)
) {
    public WAGroupFilterRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // Limite máximo
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "ASC";
    }
}
