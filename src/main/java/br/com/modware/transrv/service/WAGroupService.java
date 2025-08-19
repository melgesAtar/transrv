package br.com.modware.transrv.service;

import br.com.modware.transrv.model.InstanceEvolution;
import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.model.WAGroup;
import br.com.modware.transrv.repository.WAGroupRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WAGroupService {
    private final WAGroupRepository WAGroupRepository;

    public WAGroupService(WAGroupRepository WAGroupRepository) {
        this.WAGroupRepository = WAGroupRepository;
    }

    public Optional<WAGroup>findByEvolutionGroupId(String groupId) {
        return WAGroupRepository.findByEvolutionGroupId(groupId);
    }


    public void updateWAConversation(WAGroup waGroup, WAConversation waConversation) {
        waGroup.setWAConversation(waConversation);
        WAGroupRepository.save(waGroup);
    }
}
