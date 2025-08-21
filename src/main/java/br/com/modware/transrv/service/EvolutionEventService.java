package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.EventEvolution;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.model.WAGroup;
import br.com.modware.transrv.model.WAMessage;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;


@Service
public class EvolutionEventService {


    private final WAGroupService groupService;
    private final WAMessageService messageService;
    private final WAContactService waContactService;
    private final WAConversationService waConversationService;
    private final AiClassifier aiClassifier;
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EvolutionEventService.class);




    public EvolutionEventService(WAGroupService groupService, WAMessageService messageService, WAContactService waContactService, WAConversationService waConversationService, AiClassifier aiClassifier) {
        this.groupService = groupService;
        this.messageService = messageService;
        this.waContactService = waContactService;
        this.waConversationService = waConversationService;
        this.aiClassifier = aiClassifier;
    }


    public void processEvent(String payloadEvent) {
        log.info("Processing Evolution event: {}", payloadEvent);

        Gson gson = new Gson();
        EventEvolution eventEvolution = gson.fromJson(payloadEvent, EventEvolution.class);

            if(IsAMessageGroup(eventEvolution))
                groupService.findByEvolutionGroupId(eventEvolution.getData().getKey().getRemoteJid())
                    .ifPresent(WAGroup -> processGroupMessage(eventEvolution, WAGroup));


    }
     private void processGroupMessage(EventEvolution eventEvolution, WAGroup WAGroup) {
        log.info("Processing message for group: " + WAGroup.getEvolutionGroupId());
        WAConversation waConversation = WAGroup.getWAConversation();

        if (waConversation == null || waConversation.getId() == null) {
            waConversation = new WAConversation();
            waConversationService.saveConversation(waConversation);
            groupService.updateWAConversation(WAGroup, waConversation);
        }
        WAContact waContact = waContactService.findOrCreateWaContact(eventEvolution.getData().getKey().getParticipant().replace("@s.whatsapp.net", "") , eventEvolution.getData().getPushName());
        WAMessage waMessage = messageService.processMessage(eventEvolution, waConversation, waContact);

        if (waMessage != null) {
            String responseOpenAi = aiClassifier.ticketClassification(waMessage.getMessageContent());
            log.info("OpenAI response: " + responseOpenAi);

        } else {
            log.warn("No message to save for group: " + WAGroup.getEvolutionGroupId());
        }
    }


    public boolean IsAMessageGroup(EventEvolution eventEvolution) {
        return eventEvolution.getData().getKey().getRemoteJid().contains("@g.us");
    }



}
