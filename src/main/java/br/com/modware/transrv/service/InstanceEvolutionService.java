package br.com.modware.transrv.service;

import br.com.modware.transrv.model.InstanceEvolution;
import br.com.modware.transrv.repository.InstanceEvolutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstanceEvolutionService {

    private final InstanceEvolutionRepository instanceEvolutionRepository;

    public InstanceEvolutionService(InstanceEvolutionRepository instanceEvolutionRepository) {
        this.instanceEvolutionRepository = instanceEvolutionRepository;
    }

    public boolean existsByInstanceName(String instanceName){
        return instanceEvolutionRepository.existsByInstanceName(instanceName);
    }

    List<InstanceEvolution> findAll() {
        return instanceEvolutionRepository.findAll();
    }
}
