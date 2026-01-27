package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Agent;
import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.model.WAGroup;
import br.com.modware.transrv.repository.AgentRepository;
import br.com.modware.transrv.repository.WAGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WAGroupService {
    private final WAGroupRepository WAGroupRepository;
    private final AgentRepository agentRepository;

    public WAGroupService(WAGroupRepository WAGroupRepository, AgentRepository agentRepository) {
        this.WAGroupRepository = WAGroupRepository;
        this.agentRepository = agentRepository;
    }

    public Optional<WAGroup>findByEvolutionGroupId(String groupId) {
        return WAGroupRepository.findByEvolutionGroupId(groupId);
    }


    public void updateWAConversation(WAGroup waGroup, WAConversation waConversation) {
        waGroup.setWAConversation(waConversation);
        WAGroupRepository.save(waGroup);
    }

    public WAGroup save(WAGroup waGroup) {
        return WAGroupRepository.save(waGroup);
    }

    @Transactional
    public WAGroup updateAgent(Long groupId, Long agentId) {
        WAGroup group = WAGroupRepository.findByIdWithAgent(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado com ID: " + groupId));
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agente não encontrado com ID: " + agentId));
        
        group.setAgent(agent);
        WAGroup saved = WAGroupRepository.save(group);
        // Recarregar com fetch join para garantir que o agent está disponível
        return WAGroupRepository.findByIdWithAgent(saved.getId())
                .orElse(saved);
    }

    @Transactional
    public WAGroup removeAgent(Long groupId) {
        WAGroup group = WAGroupRepository.findByIdWithAgent(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado com ID: " + groupId));
        
        group.setAgent(null);
        WAGroup saved = WAGroupRepository.save(group);
        // Recarregar para garantir estado consistente
        return WAGroupRepository.findByIdWithAgent(saved.getId())
                .orElse(saved);
    }

}
