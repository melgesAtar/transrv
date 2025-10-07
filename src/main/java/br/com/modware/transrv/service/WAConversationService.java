package br.com.modware.transrv.service;

import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.repository.WAConversationRepository;
import org.springframework.stereotype.Service;
import br.com.modware.transrv.model.WAContact;

@Service
public class WAConversationService {
    private  final WAConversationRepository waConversationRepository;

    public WAConversationService(WAConversationRepository waConversationRepository) {
        this.waConversationRepository = waConversationRepository;
    }

    public void saveConversation(WAConversation waConversation) {
        waConversationRepository.save(waConversation);
    }

    public WAConversation findOrCreatePrivateConversation(WAContact waContact) {
        return waConversationRepository.findByWaContact(waContact)
                .orElseGet(() -> {
                    WAConversation newConversation = new WAConversation();
                    newConversation.setWaContact(waContact);
                    return waConversationRepository.save(newConversation);
                });
    }
}
