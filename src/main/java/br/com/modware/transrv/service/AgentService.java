package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Agent;
import br.com.modware.transrv.repository.AgentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AgentService {
    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    Optional<Agent> findByName(String name) {
        return agentRepository.findByName(name);
    }
}
