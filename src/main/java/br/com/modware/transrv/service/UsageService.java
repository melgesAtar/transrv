package br.com.modware.transrv.service;

import br.com.modware.transrv.model.AIUsage;

import br.com.modware.transrv.repository.UsageRepository;
import org.springframework.stereotype.Service;

@Service
public class UsageService {
    private final UsageRepository usageRepository;

    public UsageService(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    public void saveUsage(AIUsage usage) {
        usageRepository.save(usage);
    }

}
