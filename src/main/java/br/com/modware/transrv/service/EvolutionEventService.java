package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.EventEvolution;
import br.com.modware.transrv.dto.openai.AiClassifierResponse;
import br.com.modware.transrv.dto.openai.ResponseClassifierMessage;
import br.com.modware.transrv.model.*;
import com.google.gson.Gson;
import org.apache.poi.ss.formula.functions.T;
import org.quartz.SchedulerException;
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
    private final AIUsageService aiUsageService;
    private final WAMessageService waMessageService;
    private final AlertTermsService alertTermsService;
    private final TicketService ticketService;

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EvolutionEventService.class);


    public EvolutionEventService(WAGroupService groupService, WAMessageService messageService, WAContactService waContactService, WAConversationService waConversationService, AiClassifier aiClassifier, AIUsageService aiUsageService, WAMessageService waMessageService, AlertTermsService alertTermsService, TicketService ticketService) {
        this.groupService = groupService;
        this.messageService = messageService;
        this.waContactService = waContactService;
        this.waConversationService = waConversationService;
        this.aiClassifier = aiClassifier;
        this.aiUsageService = aiUsageService;
        this.waMessageService = waMessageService;
        this.alertTermsService = alertTermsService;
        this.ticketService = ticketService;
    }


    public void processEvent(String payloadEvent) {
        // Reduz ruído em produção (payload pode ser grande)
        log.debug("Evolution event recebido");

        Gson gson = new Gson();
        EventEvolution eventEvolution = gson.fromJson(payloadEvent, EventEvolution.class);

            if(IsAMessageGroup(eventEvolution))
                groupService.findByEvolutionGroupId(eventEvolution.getData().getKey().getRemoteJid())
                    .ifPresent(WAGroup -> {
                        try {
                            processGroupMessage(eventEvolution, WAGroup);
                        } catch (SchedulerException e) {
                            throw new RuntimeException(e);
                        }
                    });


    }
    private void processGroupMessage(EventEvolution eventEvolution, WAGroup WAGroup) throws SchedulerException {
        // Mantém log conciso de contexto do grupo
        log.info("Mensagem recebida | grupo={}({})", WAGroup.getGroupName(), WAGroup.getId());

        WAConversation waConversation = WAGroup.getWAConversation();
        if (waConversation == null || waConversation.getId() == null) {
            waConversation = new WAConversation();
            waConversationService.saveConversation(waConversation);
            groupService.updateWAConversation(WAGroup, waConversation);
        }

        WAContact waContact = waContactService.findOrCreateWaContact(
                eventEvolution.getData().getKey().getParticipant().replace("@s.whatsapp.net", ""),
                eventEvolution.getData().getPushName()
        );

        // Log com usuário e tipo de mensagem
        String messageType = eventEvolution.getData().getMessageType();
        log.info("Mensagem recebida do usuário={}({}) | tipo={}",
                waContact.getName(), waContact.getPhoneNumber(), messageType);

        WAMessage waMessage = messageService.processMessage(eventEvolution, waConversation, waContact);

        boolean closed = ticketService.tryCloseTicket(
                eventEvolution.getData().getContextInfo() != null ? eventEvolution.getData().getContextInfo().getStanzaId() : null,
                waMessage.getMessageContent(),
                waContact,
                waMessage
        );

        if (closed) {
            log.info("Ticket fechado pela mensagem recebida no grupo={} por usuário={}({})",
                    WAGroup.getGroupName(), waContact.getName(), waContact.getPhoneNumber());
            return;
        }


        ResponseClassifierMessage responseOpenAi = aiClassifier.ticketClassification(waMessage.getMessageContent());
        String contentJson = responseOpenAi.getChoices().get(0).getMessage().getContent();
        Gson gson = new Gson();
        AiClassifierResponse classifierResponse = gson.fromJson(contentJson, AiClassifierResponse.class);

        // Uso de AI pode ser ruidoso; mover para DEBUG
        log.debug("AI Usage: {}", responseOpenAi.getUsage());
        AIUsage aiUsage = new AIUsage(
                responseOpenAi.getUsage().getPromptTokens(),
                responseOpenAi.getUsage().getCompletionTokens(),
                responseOpenAi.getUsage().getTotalTokens(),
                LocalDateTime.now(ZoneId.of("America/Sao_Paulo")),
                waMessage
        );
        aiUsageService.saveUsage(aiUsage);

        if (classifierResponse.isShouldOpen()) {
            String alertCode = classifierResponse.getAlertTerm();

            alertTermsService.findActiveByCode(alertCode)
                    .ifPresentOrElse(alertTerm -> {
                        waMessage.setAlertTerm(alertTerm);
                        waMessageService.saveMessage(waMessage);
                        try {
                            Ticket ticket = ticketService.openTicket(waMessage, alertTerm, waContact, WAGroup);
                            log.info("Ticket aberto | id={} | alerta={} | grupo={}",
                                    ticket.getId(), alertTerm.getCode(), WAGroup.getGroupName());
                        } catch (SchedulerException e) {
                            throw new RuntimeException(e);
                        }
                    }, () -> log.warn("AlertTerm código '{}' não encontrado ou INACTIVE", alertCode));
        } else {
            // Mensagem não necessita de abertura de ticket
            String content = waMessage.getMessageContent();
            if (content != null && content.length() > 120) {
                content = content.substring(0, 120) + "...";
            }
            log.info("Mensagem não necessita de abertura de ticket | grupo={} | usuário={}({}) | conteúdo={}",
                    WAGroup.getGroupName(), waContact.getName(), waContact.getPhoneNumber(), content);
        }
    }


    public boolean IsAMessageGroup(EventEvolution eventEvolution) {
        return eventEvolution.getData().getKey().getRemoteJid().contains("@g.us");
    }



}
