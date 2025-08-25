package br.com.modware.transrv.service;

import br.com.modware.transrv.model.AIUsage;

import br.com.modware.transrv.repository.AIUsageRepository;
import br.com.modware.transrv.repository.UsageRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class AIUsageService {
    Logger log = org.slf4j.LoggerFactory.getLogger(AIUsageService.class);
    private final AIUsageRepository aiUsageRepository;

    public AIUsageService(AIUsageRepository aiUsageRepository) {
        this.aiUsageRepository = aiUsageRepository;
    }


    public void saveUsage(AIUsage usage) {
        if (aiUsageRepository.existsByMessage(usage.getMessage())) {
            log.info("AI Usage for messageId {} already exists, skipping", usage.getMessage());
            return;
        }
        aiUsageRepository.save(usage);
    }


}
