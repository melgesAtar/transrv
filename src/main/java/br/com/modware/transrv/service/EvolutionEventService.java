package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.evolution.EventEvolution;
import br.com.modware.transrv.dto.openai.AiClassifierResponse;
import br.com.modware.transrv.dto.openai.ResponseClassifierMessage;
import br.com.modware.transrv.model.*;
import br.com.modware.transrv.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;

    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EvolutionEventService.class);


    public EvolutionEventService(WAGroupService groupService, WAMessageService messageService, WAContactService waContactService, WAConversationService waConversationService, AiClassifier aiClassifier, AIUsageService aiUsageService, WAMessageService waMessageService, AlertTermsService alertTermsService, TicketService ticketService, EvolutionApiClient evolutionApiClient, EmployeeRepository employeeRepository) {
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
        this.employeeRepository = employeeRepository;
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

        if (waMessage.getMessageContent() == null || waMessage.getMessageContent().isBlank()) {
            log.warn("Mensagem vazia, pulando classificação para evitar erro na OpenAI");
            return;
        }
        Agent groupAgent = WAGroup.getAgent();
        ResponseClassifierMessage responseOpenAi = aiClassifier.ticketClassification(waMessage.getMessageContent(), groupAgent);
        String contentJson = responseOpenAi.getChoices().get(0).getMessage().getContent();
        Gson gson = new Gson();

        log.info("ChatGPT content: {}", contentJson);
        AiClassifierResponse classifierResponse = gson.fromJson(contentJson, AiClassifierResponse.class);

        // Vincular funcionário se veio do classificador
        Employee resolvedEmployee = null;
        String employeeName = classifierResponse.getEmployee();
        if (employeeName != null && !employeeName.isBlank()) {
            String phone = waContact.getPhoneNumber();
            log.info("AI sugeriu employee='{}' para telefone {} (contato: {} - {})",
                    employeeName, phone, waContact.getName(), waContact.getId());
            // Primeiro: match direto por telefone + nome (ignore case)
            java.util.List<Employee> directMatches = employeeRepository.findByWaContact_PhoneNumberAndNameIgnoreCase(phone, employeeName);
            if (directMatches != null && !directMatches.isEmpty()) {
                log.info("Match direto telefone+nome encontrou {} funcionário(s): {}",
                        directMatches.size(),
                        directMatches.stream().map(Employee::getName).reduce((a,b) -> a + ", " + b).orElse(""));
                resolvedEmployee = directMatches.get(0);
            } else {
                // Fallback: buscar por telefone e tentar resolver desambiguação
                java.util.List<Employee> byPhone = employeeRepository.findByWaContact_PhoneNumber(phone);
                if (byPhone != null && !byPhone.isEmpty()) {
                    log.info("Busca por telefone encontrou {} funcionário(s) para {}: {}",
                            byPhone.size(), phone,
                            byPhone.stream().map(Employee::getName).reduce((a,b) -> a + ", " + b).orElse(""));
                    java.util.List<Employee> matches = byPhone.stream()
                            .filter(e -> e.getName() != null && e.getName().equalsIgnoreCase(employeeName))
                            .toList();
                    if (!matches.isEmpty()) {
                        resolvedEmployee = matches.get(0);
                    } else if (byPhone.size() == 1) {
                        log.info("Sem match exato de nome, usando único funcionário pelo telefone");
                        resolvedEmployee = byPhone.get(0);
                    } else {
                        log.warn("Ambiguidade: múltiplos funcionários para telefone {} e nenhum match exato por nome='{}'",
                                phone, employeeName);
                    }
                }
            }
            if (resolvedEmployee != null) {
                waMessage.setEmployee(resolvedEmployee);
                WAMessage savedMsg = waMessageService.saveMessage(waMessage);
                log.info("Mensagem vinculada ao funcionário | messageId={} | employeeId={} | employeeName={}",
                        savedMsg != null ? savedMsg.getId() : null,
                        resolvedEmployee.getId(),
                        resolvedEmployee.getName());
            } else {
                log.info("Nenhum funcionário resolvido para phone={} e nome sugerido='{}'", phone, employeeName);
            }
        }

        log.debug("AI Usage: {}", responseOpenAi.getUsage());
        AIUsage aiUsage = new AIUsage(
                responseOpenAi.getUsage().getPromptTokens(),
                responseOpenAi.getUsage().getCompletionTokens(),
                responseOpenAi.getUsage().getTotalTokens(),
                LocalDateTime.now(ZoneId.of("America/Sao_Paulo")),
                waMessage
        );
        aiUsageService.saveUsage(aiUsage);

        final Employee employeeForTicket = resolvedEmployee;

        if (classifierResponse.isShouldOpen()) {
            String alertCode = classifierResponse.getAlertTerm();

            alertTermsService.findActiveByCode(alertCode)
                    .ifPresentOrElse(alertTerm -> {
                        waMessage.setAlertTerm(alertTerm);
                        waMessageService.saveMessage(waMessage);
                        try {
                            Ticket ticket = ticketService.openTicket(waMessage, alertTerm, waContact, WAGroup, employeeForTicket);
                            if (employeeForTicket != null) {
                                log.info("Ticket aberto | id={} | alerta={} | grupo={} | employeeId={} | employeeName={}",
                                        ticket.getId(), alertTerm.getCode(), WAGroup.getGroupName(),
                                        employeeForTicket.getId(), employeeForTicket.getName());
                            } else {
                                log.info("Ticket aberto | id={} | alerta={} | grupo={} | sem funcionário associado",
                                        ticket.getId(), alertTerm.getCode(), WAGroup.getGroupName());
                            }
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

        // Após classificador, tentar fechar (assim o employee pode ter sido resolvido na WAMessage)
        boolean closed = ticketService.tryCloseTicket(
                eventEvolution.getData().getContextInfo() != null ? eventEvolution.getData().getContextInfo().getStanzaId() : null,
                waMessage.getMessageContent(),
                waContact,
                waMessage
        );

        if (closed) {
            log.info("Ticket fechado pela mensagem recebida no grupo={} por usuário={}({})",
                    WAGroup.getGroupName(), waContact.getName(), waContact.getPhoneNumber());
        }
    }


    public boolean IsAMessageGroup(EventEvolution eventEvolution) {
        return eventEvolution.getData().getKey().getRemoteJid().contains("@g.us");
    }



}
