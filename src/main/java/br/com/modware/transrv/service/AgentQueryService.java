package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.agent.AgentFilterRequest;
import br.com.modware.transrv.dto.agent.AgentPageResponse;
import br.com.modware.transrv.dto.agent.AgentResponse;
import br.com.modware.transrv.model.Agent;
import br.com.modware.transrv.repository.AgentRepository;
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
public class AgentQueryService {

    private final AgentRepository agentRepository;

    public AgentQueryService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Transactional(readOnly = true)
    public AgentPageResponse findAgents(AgentFilterRequest filter) {
        
        // Construir Specification com filtros dinâmicos
        Specification<Agent> spec = buildSpecification(filter);

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
        Page<Agent> agentPage = agentRepository.findAll(spec, pageable);

        // Converter para DTOs
        return new AgentPageResponse(
            agentPage.getContent().stream()
                .map(AgentResponse::from)
                .toList(),
            agentPage.getNumber(),
            agentPage.getSize(),
            agentPage.getTotalElements(),
            agentPage.getTotalPages(),
            agentPage.isFirst(),
            agentPage.isLast()
        );
    }

    private Specification<Agent> buildSpecification(AgentFilterRequest filter) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
