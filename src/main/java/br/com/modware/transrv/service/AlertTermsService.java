package br.com.modware.transrv.service;

import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.repository.AlertTermRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlertTermsService {
    private final AlertTermRepository alertTermRepository;

    public AlertTermsService(AlertTermRepository alertTermRepository) {
        this.alertTermRepository = alertTermRepository;
    }

    public List<AlertTerm> findAllActiveAlertTerms() {
        return alertTermRepository.findAllByStatus(AlertTerm.Status.ACTIVE);
    }

    public Optional<AlertTerm> findActiveByCode(String code) {
        if (code == null)
            return Optional.empty();
        return alertTermRepository.findByCodeAndStatus(code.trim().toUpperCase(), AlertTerm.Status.ACTIVE);
    }

    @Transactional
    public AlertTerm updateDescription(Long id, String description) {
        AlertTerm alertTerm = alertTermRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Termo de alerta não encontrado com ID: " + id));

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode ser vazia");
        }

        if (description.length() > 120) {
            throw new IllegalArgumentException("Descrição não pode ter mais de 120 caracteres");
        }

        alertTerm.setDescription(description.trim());
        return alertTermRepository.save(alertTerm);
    }

    @Transactional
    public AlertTerm disable(Long id) {
        AlertTerm alertTerm = alertTermRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Termo de alerta não encontrado com ID: " + id));

        alertTerm.setStatus(AlertTerm.Status.INACTIVE);
        return alertTermRepository.save(alertTerm);
    }
}
