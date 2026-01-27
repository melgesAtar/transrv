package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.group.WAGroupFilterRequest;
import br.com.modware.transrv.dto.group.WAGroupPageResponse;
import br.com.modware.transrv.dto.group.WAGroupResponse;
import br.com.modware.transrv.model.WAGroup;
import br.com.modware.transrv.repository.WAGroupRepository;
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
public class WAGroupQueryService {

    private final WAGroupRepository waGroupRepository;

    public WAGroupQueryService(WAGroupRepository waGroupRepository) {
        this.waGroupRepository = waGroupRepository;
    }

    @Transactional(readOnly = true)
    public WAGroupPageResponse findGroups(WAGroupFilterRequest filter) {
        
        // Construir Specification com filtros dinâmicos
        Specification<WAGroup> spec = buildSpecification(filter);

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
        Page<WAGroup> groupPage = waGroupRepository.findAll(
            addFetchJoins(spec), 
            pageable
        );

        // Converter para DTOs
        return new WAGroupPageResponse(
            groupPage.getContent().stream()
                .map(WAGroupResponse::from)
                .toList(),
            groupPage.getNumber(),
            groupPage.getSize(),
            groupPage.getTotalElements(),
            groupPage.getTotalPages(),
            groupPage.isFirst(),
            groupPage.isLast()
        );
    }

    private Specification<WAGroup> buildSpecification(WAGroupFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nome do grupo (busca parcial, case-insensitive)
            if (filter.groupName() != null && !filter.groupName().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("groupName")),
                        "%" + filter.groupName().toLowerCase() + "%"
                    )
                );
            }

            // Filtro por evolutionGroupId (busca exata)
            if (filter.evolutionGroupId() != null && !filter.evolutionGroupId().isBlank()) {
                predicates.add(
                    cb.equal(root.get("evolutionGroupId"), filter.evolutionGroupId())
                );
            }

            // Filtro por agente
            if (filter.agentId() != null) {
                predicates.add(
                    cb.equal(root.get("agent").get("id"), filter.agentId())
                );
            }

            // Filtro por status de monitoramento
            if (filter.isMonitored() != null) {
                predicates.add(
                    cb.equal(root.get("isMonitored"), filter.isMonitored())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<WAGroup> addFetchJoins(Specification<WAGroup> spec) {
        return spec.and((root, query, cb) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return null;
            }
            
            root.fetch("agent", jakarta.persistence.criteria.JoinType.LEFT);
            
            return null;
        });
    }
}
