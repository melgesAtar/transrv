package br.com.modware.transrv.service;

import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.repository.AlertTermRepository;
import org.springframework.stereotype.Service;

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
        if (code == null) return Optional.empty();
        return alertTermRepository.findByCodeAndStatus(code.trim().toUpperCase(), AlertTerm.Status.ACTIVE);
    }
    }

