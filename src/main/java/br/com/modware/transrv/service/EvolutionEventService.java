package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.EventEvolution;
import br.com.modware.transrv.dto.openai.AiClassifierResponse;
import br.com.modware.transrv.dto.openai.ResponseClassifierMessage;
import br.com.modware.transrv.model.*;
import com.google.gson.Gson;
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
    private final EvolutionApiClient evolutionApiClient;

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EvolutionEventService.class);


    public EvolutionEventService(WAGroupService groupService, WAMessageService messageService, WAContactService waContactService, WAConversationService waConversationService, AiClassifier aiClassifier, AIUsageService aiUsageService, WAMessageService waMessageService, AlertTermsService alertTermsService, TicketService ticketService, EvolutionApiClient evolutionApiClient) {
        this.groupService = groupService;
        this.messageService = messageService;
        this.waContactService = waContactService;
        this.waConversationService = waConversationService;
        this.aiClassifier = aiClassifier;
        this.aiUsageService = aiUsageService;
        this.waMessageService = waMessageService;
        this.alertTermsService = alertTermsService;
        this.ticketService = ticketService;
        this.evolutionApiClient = evolutionApiClient;
    }


    public void processEvent(String payloadEvent) {
        log.debug("Evolution event recebido");

        Gson gson = new Gson();
        EventEvolution eventEvolution = gson.fromJson(payloadEvent, EventEvolution.class);

            if(IsAMessageGroup(eventEvolution)) {
                String remoteJid = eventEvolution.getData().getKey().getRemoteJid();
                log.info("Evento de mensagem em grupo recebido | grupoId={}", remoteJid);

                WAGroup waGroup = groupService.findByEvolutionGroupId(remoteJid)
                        .orElseGet(() -> {
                            WAGroup newGroup = new WAGroup();
                            newGroup.setEvolutionGroupId(remoteJid);
                            // Busca o nome real do grupo (subject) na Evolution API usando serverURL/instance do evento
                            try {
                                String serverUrl = eventEvolution.getServerURL();
                                String instanceName = eventEvolution.getInstance();
                                log.info("Criando WAGroup novo | remoteJid={} serverUrl={} instance={}", remoteJid, serverUrl, instanceName);
                                if (serverUrl != null && instanceName != null) {
                                    var info = evolutionApiClient.fetchGroupInfo(serverUrl, instanceName, remoteJid);
                                    String subject = info != null ? info.getSubject() : null;
                                    newGroup.setGroupName(subject != null && !subject.isBlank() ? subject : remoteJid);
                                    log.info("Subject resolvido para grupo {} => {}", remoteJid, newGroup.getGroupName());
                                } else {
                                    newGroup.setGroupName(remoteJid);
                                    log.warn("ServerURL ou instance nulos no evento; usando remoteJid como nome do grupo");
                                }
                            } catch (Exception e) {
                                newGroup.setGroupName(remoteJid);
                                log.error("Falha ao resolver subject via Evolution API | remoteJid={} erro={}", remoteJid, e.getMessage());
                            }
                            newGroup.setMonitored(false);

                            WAConversation conversation = new WAConversation();
                            waConversationService.saveConversation(conversation);
                            newGroup.setWAConversation(conversation);

                            return groupService.save(newGroup);
                        });

                if (!waGroup.isMonitored()) {
                    log.info("Grupo não monitorado, ignorando mensagem | grupo={}({})", waGroup.getGroupName(), waGroup.getEvolutionGroupId());
                    return;
                }

                try {
                    processGroupMessage(eventEvolution, waGroup);
                } catch (SchedulerException e) {
                    throw new RuntimeException(e);
                }
            }


    }
    private void processGroupMessage(EventEvolution eventEvolution, WAGroup WAGroup) throws SchedulerException {
        if (!WAGroup.isMonitored()) {
            log.debug("processGroupMessage chamado para grupo não monitorado | grupo={}({})", WAGroup.getGroupName(), WAGroup.getEvolutionGroupId());
            return;
        }
        log.info("Mensagem recebida | grupo={}({})", WAGroup.getGroupName(), WAGroup.getEvolutionGroupId());

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

        String messageType = eventEvolution.getData().getMessageType();
        log.info("Mensagem recebida do usuário={}({}) | tipo={}",
                waContact.getName(), waContact.getPhoneNumber(), messageType);

        WAMessage waMessage = messageService.processMessage(eventEvolution, waConversation, waContact, WAGroup);

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
