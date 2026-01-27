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


    private String generateCode(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio para gerar o código");
        }

        String code = name.trim()
                .toUpperCase()
                .replaceAll("[áàâãä]", "A")
                .replaceAll("[éèêë]", "E")
                .replaceAll("[íìîï]", "I")
                .replaceAll("[óòôõö]", "O")
                .replaceAll("[úùûü]", "U")
                .replaceAll("[ç]", "C")
                .replaceAll("[ñ]", "N")
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_+|_+$", "");

       
        if (code.length() > 64) {
            code = code.substring(0, 64);
            code = code.replaceAll("_+$", "");
        }

        if (code.isEmpty()) {
            throw new IllegalArgumentException("Não foi possível gerar um código válido a partir do nome");
        }

        return code;
    }


    private String generateUniqueCode(String baseCode) {
        String code = baseCode;
        int suffix = 1;

        while (alertTermRepository.findByCode(code).isPresent()) {
            String suffixStr = "_" + suffix;
            int maxLength = 256 - suffixStr.length();
            if (maxLength <= 0) {
                throw new IllegalArgumentException("Não foi possível gerar um código único");
            }
            code = baseCode.substring(0, Math.min(baseCode.length(), maxLength)) + suffixStr;
            suffix++;
        }

        return code;
    }

    @Transactional
    public AlertTerm create(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode ser vazia");
        }

        if (name.length() > 120) {
            throw new IllegalArgumentException("Nome não pode ter mais de 120 caracteres");
        }

        if (description.length() > 120) {
            throw new IllegalArgumentException("Descrição não pode ter mais de 120 caracteres");
        }

        String baseCode = generateCode(name);
        String uniqueCode = generateUniqueCode(baseCode);

        AlertTerm alertTerm = new AlertTerm();
        alertTerm.setName(name.trim());
        alertTerm.setDescription(description.trim());
        alertTerm.setCode(uniqueCode);
        alertTerm.setStatus(AlertTerm.Status.ACTIVE);

        return alertTermRepository.save(alertTerm);
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
    public AlertTerm toggle(Long id) {
        AlertTerm alertTerm = alertTermRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Termo de alerta não encontrado com ID: " + id));

        if (alertTerm.getStatus() == AlertTerm.Status.ACTIVE) {
            alertTerm.setStatus(AlertTerm.Status.INACTIVE);
        } else {
            alertTerm.setStatus(AlertTerm.Status.ACTIVE);
        }
        
        return alertTermRepository.save(alertTerm);
    }
}
