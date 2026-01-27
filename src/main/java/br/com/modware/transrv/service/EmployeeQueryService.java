package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.employee.EmployeeFilterRequest;
import br.com.modware.transrv.dto.employee.EmployeePageResponse;
import br.com.modware.transrv.dto.employee.EmployeeResponse;
import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeQueryService {

    private final EmployeeRepository employeeRepository;

    public EmployeeQueryService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public EmployeePageResponse findEmployees(EmployeeFilterRequest filter) {
        
        // Construir Specification com filtros dinâmicos
        Specification<Employee> spec = buildSpecification(filter);

        // Configurar ordenação
        Sort sort = Sort.by(
            "DESC".equalsIgnoreCase(filter.sortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            filter.sortBy()
        );
        
        // Criar Pageable
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

        // Executar query paginada com fetch joins
        Page<Employee> employeePage = employeeRepository.findAll(
            addFetchJoins(spec), 
            pageable
        );

        // Converter para DTOs
        return new EmployeePageResponse(
            employeePage.getContent().stream()
                .map(EmployeeResponse::from)
                .toList(),
            employeePage.getNumber(),
            employeePage.getSize(),
            employeePage.getTotalElements(),
            employeePage.getTotalPages(),
            employeePage.isFirst(),
            employeePage.isLast()
        );
    }

    private Specification<Employee> buildSpecification(EmployeeFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nome (busca parcial, case-insensitive)
            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.name().toLowerCase() + "%"
                    )
                );
            }

            // Filtro por departamento
            if (filter.departmentId() != null) {
                predicates.add(
                    cb.equal(root.get("department").get("id"), filter.departmentId())
                );
            }

            // Filtro por status ativo/inativo
            if (filter.isActive() != null) {
                predicates.add(
                    cb.equal(root.get("isActive"), filter.isActive())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Employee> addFetchJoins(Specification<Employee> spec) {
        return spec.and((root, query, cb) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return null;
            }
            
            root.fetch("department", jakarta.persistence.criteria.JoinType.LEFT);
            
            return null;
        });
    }
}
