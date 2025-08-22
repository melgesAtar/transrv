package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.EventEvolution;
import br.com.modware.transrv.dto.openai.AiClassifierResponse;
import br.com.modware.transrv.dto.openai.ResponseClassifierMessage;
import br.com.modware.transrv.model.*;
import com.google.gson.Gson;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Service
public class EvolutionEventService {


    private final WAGroupService groupService;
    private final WAMessageService messageService;
    private final WAContactService waContactService;
    private final WAConversationService waConversationService;
    private final AiClassifier aiClassifier;
    private final UsageService usageService;
    private final WAMessageService waMessageService;
    private final AlertTermsService alertTermsService;
    private final TicketService ticketService;

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EvolutionEventService.class);


    public EvolutionEventService(WAGroupService groupService, WAMessageService messageService, WAContactService waContactService, WAConversationService waConversationService, AiClassifier aiClassifier, UsageService usageService, WAMessageService waMessageService, AlertTermsService alertTermsService, TicketService ticketService) {
        this.groupService = groupService;
        this.messageService = messageService;
        this.waContactService = waContactService;
        this.waConversationService = waConversationService;
        this.aiClassifier = aiClassifier;
        this.usageService = usageService;
        this.waMessageService = waMessageService;
        this.alertTermsService = alertTermsService;
        this.ticketService = ticketService;
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

                ResponseClassifierMessage responseOpenAi = aiClassifier.ticketClassification(waMessage.getMessageContent());
                String contentJson = responseOpenAi.getChoices().get(0).getMessage().getContent();
                Gson gson = new Gson();
                AiClassifierResponse classifierResponse = gson.fromJson(contentJson, AiClassifierResponse.class);

                log.info("AI Usage: " + responseOpenAi.getUsage());

                AIUsage aiUsage =   new AIUsage(responseOpenAi.getUsage().getPromptTokens(), responseOpenAi.getUsage().getCompletionTokens(),
                    responseOpenAi.getUsage().getTotalTokens() , LocalDateTime.now(ZoneId.of("America/Sao_Paulo")), waMessage);
                    usageService.saveUsage(aiUsage);


            if (classifierResponse.isShouldOpen()) {
                String alertCode = classifierResponse.getAlertTerm();

                alertTermsService.findActiveByCode(alertCode)
                        .ifPresentOrElse(alertTerm -> {
                            waMessage.setAlertTerm(alertTerm);
                            waMessageService.saveMessage(waMessage);
                           Ticket ticket=  ticketService.openTicket(waMessage, alertTerm, waContact, WAGroup);
                        }, () -> {
                            log.warn("AlertTerm código '{}' não encontrado ou INACTIVE", alertCode);
                        });
            }



        } else {
            log.warn("No message to save for group: " + WAGroup.getEvolutionGroupId());
        }
    }


    public boolean IsAMessageGroup(EventEvolution eventEvolution) {
        return eventEvolution.getData().getKey().getRemoteJid().contains("@g.us");
    }



}
