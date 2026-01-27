package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.ticket.TicketFilterRequest;
import br.com.modware.transrv.dto.ticket.TicketPageResponse;
import br.com.modware.transrv.dto.ticket.TicketResponse;
import br.com.modware.transrv.model.Ticket;
import br.com.modware.transrv.repository.TicketRepository;
import br.com.modware.transrv.repository.specification.TicketSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketQueryService {

    private final TicketRepository ticketRepository;

    public TicketQueryService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public TicketPageResponse findTickets(TicketFilterRequest filter) {
        
        Specification<Ticket> spec = TicketSpecification.buildSpecification(
            filter.groupId(),
            filter.escalationLevels(),
            filter.alertTermIds(),
            filter.status(),
            filter.startDate(),
            filter.endDate(),
            filter.status() != null && filter.status() != Ticket.Status.OPEN
        );

        
        Sort sort = Sort.by(
            "DESC".equalsIgnoreCase(filter.sortDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            filter.sortBy()
        );
        
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

       
        Page<Ticket> ticketPage = ticketRepository.findAll(
            addFetchJoins(spec), 
            pageable
        );

        return new TicketPageResponse(
            ticketPage.getContent().stream()
                .map(TicketResponse::from)
                .toList(),
            ticketPage.getNumber(),
            ticketPage.getSize(),
            ticketPage.getTotalElements(),
            ticketPage.getTotalPages(),
            ticketPage.isFirst(),
            ticketPage.isLast()
        );
    }

  
    @Transactional(readOnly = true)
    public TicketResponse findTicketById(Long id) {
        Specification<Ticket> spec = (root, query, cb) -> cb.equal(root.get("id"), id);
        
        Specification<Ticket> specWithFetch = addFetchJoins(spec);
        
        return ticketRepository.findOne(specWithFetch)
                .map(TicketResponse::from)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ticket não encontrado com ID: " + id));
    }

    private Specification<Ticket> addFetchJoins(Specification<Ticket> spec) {
        return spec.and((root, query, cb) -> {
            
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return null; 
            }
            
         
            root.fetch("waGroup", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("alertTerm", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("messageResponsibleForOpeningTheCall", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("contactResponsibleForOpeningTheCall", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("employeeResponsibleForOpeningTheCall", jakarta.persistence.criteria.JoinType.LEFT);
            
            return null;
        });
    }
}