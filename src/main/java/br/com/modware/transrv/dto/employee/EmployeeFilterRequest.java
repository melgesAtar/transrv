package br.com.modware.transrv.dto.employee;

public record EmployeeFilterRequest(
    String name,                   // Filtro por nome (busca parcial, case-insensitive)
    Long departmentId,             // Filtro por ID do departamento
    Boolean isActive,               // Filtro por status ativo/inativo
    Integer page,                   // Número da página (base 0)
    Integer size,                   // Tamanho da página
    String sortBy,                  // Campo para ordenação
    String sortDirection            // Direção da ordenação (ASC ou DESC)
) {
    public EmployeeFilterRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // Limite máximo
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "ASC";
    }
}
