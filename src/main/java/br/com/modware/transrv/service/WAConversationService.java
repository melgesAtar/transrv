package br.com.modware.transrv.service;

import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.repository.WAConversationRepository;
import org.springframework.stereotype.Service;

@Service
public class WAConversationService {
    private  final WAConversationRepository waConversationRepository;

    public WAConversationService(WAConversationRepository waConversationRepository) {
        this.waConversationRepository = waConversationRepository;
    }

    public void saveConversation(WAConversation waConversation) {
        waConversationRepository.save(waConversation);
    }
}
