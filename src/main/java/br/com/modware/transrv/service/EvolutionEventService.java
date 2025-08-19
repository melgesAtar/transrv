package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.EventEvolution;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.model.WAConversation;
import br.com.modware.transrv.model.WAGroup;
import br.com.modware.transrv.model.WAMessage;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

@Service
public class EvolutionEventService {

    private final InstanceEvolutionService instanceEvolutionService;
    private final WAGroupService groupService;
    private final WAMessageService messageService;
    private final WAContactService waContactService;
    private final WAConversationService waConversationService;
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EvolutionEventService.class);




    public EvolutionEventService(InstanceEvolutionService instanceEvolutionService, WAGroupService groupService, WAMessageService messageService, WAContactService waContactService, WAConversationService waConversationService) {
        this.instanceEvolutionService = instanceEvolutionService;
        this.groupService = groupService;
        this.messageService = messageService;
        this.waContactService = waContactService;
        this.waConversationService = waConversationService;
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


    }


    public boolean IsAMessageGroup(EventEvolution eventEvolution) {
        return eventEvolution.getData().getKey().getRemoteJid().contains("@g.us");
    }



}
