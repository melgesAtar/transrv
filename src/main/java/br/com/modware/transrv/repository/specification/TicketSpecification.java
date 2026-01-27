// repository/specification/TicketSpecification.java
package br.com.modware.transrv.repository.specification;

import br.com.modware.transrv.model.Ticket;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> hasGroup(Long groupId) {
        return (root, query, cb) -> {
            if (groupId == null) return cb.conjunction();
            return cb.equal(root.get("waGroup").get("id"), groupId);
        };
    }

    public static Specification<Ticket> hasEscalationLevels(List<Integer> levels) {
        return (root, query, cb) -> {
            if (levels == null || levels.isEmpty()) return cb.conjunction();
            return root.get("currentEscalationLevel").in(levels);
        };
    }

    public static Specification<Ticket> hasAlertTerms(List<Long> alertTermIds) {
        return (root, query, cb) -> {
            if (alertTermIds == null || alertTermIds.isEmpty()) return cb.conjunction();
            return root.get("alertTerm").get("id").in(alertTermIds);
        };
    }

    public static Specification<Ticket> hasStatus(Ticket.Status status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Ticket> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return cb.conjunction();
            
            Path<LocalDateTime> createdAt = root.get("createdAt");
            
            if (startDate != null && endDate != null) {
                return cb.between(createdAt, startDate, endDate);
            } else if (startDate != null) {
                return cb.greaterThanOrEqualTo(createdAt, startDate);
            } else {
                return cb.lessThanOrEqualTo(createdAt, endDate);
            }
        };
    }

    public static Specification<Ticket> closedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return cb.conjunction();
            
            Path<LocalDateTime> closedAt = root.get("closedAt");
            
            if (startDate != null && endDate != null) {
                return cb.between(closedAt, startDate, endDate);
            } else if (startDate != null) {
                return cb.greaterThanOrEqualTo(closedAt, startDate);
            } else {
                return cb.lessThanOrEqualTo(closedAt, endDate);
            }
        };
    }

    // Combina todas as specifications
    public static Specification<Ticket> buildSpecification(
            Long groupId,
            List<Integer> escalationLevels,
            List<Long> alertTermIds,
            Ticket.Status status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            boolean filterByClosedDate) {
        
        Specification<Ticket> spec = Specification.where(null);
        
        spec = spec.and(hasGroup(groupId));
        spec = spec.and(hasEscalationLevels(escalationLevels));
        spec = spec.and(hasAlertTerms(alertTermIds));
        spec = spec.and(hasStatus(status));
        
        if (filterByClosedDate && status != null && status != Ticket.Status.OPEN) {
            spec = spec.and(closedBetween(startDate, endDate));
        } else {
            spec = spec.and(createdBetween(startDate, endDate));
        }
        
        return spec;
    }
}