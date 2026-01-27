package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.alert.AlertTermFilterRequest;
import br.com.modware.transrv.dto.alert.AlertTermPageResponse;
import br.com.modware.transrv.dto.alert.AlertTermResponse;
import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.repository.AlertTermRepository;
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
public class AlertTermQueryService {

    private final AlertTermRepository alertTermRepository;

    public AlertTermQueryService(AlertTermRepository alertTermRepository) {
        this.alertTermRepository = alertTermRepository;
    }

    @Transactional(readOnly = true)
    public AlertTermPageResponse findAlertTerms(AlertTermFilterRequest filter) {
        
        // Construir Specification com filtros dinâmicos
        Specification<AlertTerm> spec = buildSpecification(filter);

        // Configurar ordenação
        Sort sort = Sort.by(
            "DESC".equalsIgnoreCase(filter.sortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            filter.sortBy()
        );
        
        // Criar Pageable
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

        // Executar query paginada
        Page<AlertTerm> alertTermPage = alertTermRepository.findAll(spec, pageable);

        // Converter para DTOs
        return new AlertTermPageResponse(
            alertTermPage.getContent().stream()
                .map(AlertTermResponse::from)
                .toList(),
            alertTermPage.getNumber(),
            alertTermPage.getSize(),
            alertTermPage.getTotalElements(),
            alertTermPage.getTotalPages(),
            alertTermPage.isFirst(),
            alertTermPage.isLast()
        );
    }

    private Specification<AlertTerm> buildSpecification(AlertTermFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por código (busca parcial, case-insensitive)
            if (filter.code() != null && !filter.code().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("code")),
                        "%" + filter.code().toLowerCase() + "%"
                    )
                );
            }

            // Filtro por nome (busca parcial, case-insensitive)
            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.name().toLowerCase() + "%"
                    )
                );
            }

            // Filtro por status
            if (filter.status() != null) {
                predicates.add(
                    cb.equal(root.get("status"), filter.status())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
