package br.com.modware.transrv.dto.employee;

public record EmployeeCreateRequest(
    String name,
    Long departmentId  
) {
    
}
