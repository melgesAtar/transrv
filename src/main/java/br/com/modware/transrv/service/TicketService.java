package br.com.modware.transrv.service;

import br.com.modware.transrv.model.*;
import br.com.modware.transrv.quartz.EscalationJob;
import br.com.modware.transrv.quartz.ExpireTicketJob;
import br.com.modware.transrv.repository.EmployeeAlertTermRepository;
import br.com.modware.transrv.repository.TicketRepository;
import br.com.modware.transrv.repository.AlertTermRepository;
import br.com.modware.transrv.dto.dashboard.AlertNotificationDTO;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeAlertTermRepository employeeAlertTermRepository;
    private final InstanceEvolutionService instanceEvolutionService;
    private final Scheduler scheduler;
    private final TicketNotificationService ticketNotificationService;
    private final br.com.modware.transrv.repository.EmployeeWAContactRepository employeeWAContactRepository;
    private final AlertTermRepository alertTermRepository;
    private final WAContactService waContactService;
    private final WAMessageService waMessageService;
    private final WAGroupService waGroupService;
    private final TicketWebSocketService ticketWebSocketService;
    private final br.com.modware.transrv.repository.EmployeeRepository employeeRepository;

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TicketService.class);

    // Parâmetros externos (application.properties)
    @Value("${app.escalation.level2.delay-seconds:300}")
    private long escalationLevel2DelaySeconds;

    @Value("${app.escalation.level3.delay-seconds:600}")
    private long escalationLevel3DelaySeconds;

    @Value("${app.group-loop.interval-seconds:300}")
    private long groupLoopIntervalSeconds;

    @Value("${app.group-loop.initial-delay-seconds:300}")
    private long groupLoopInitialDelaySeconds;

    @Value("${app.ticket.expiration-seconds:21600}")
    private long ticketExpirationSeconds;

    public TicketService(TicketRepository ticketRepository,
                          EmployeeAlertTermRepository employeeAlertTermRepository,
                         InstanceEvolutionService instanceEvolutionService,
                         Scheduler scheduler,
                         TicketNotificationService ticketNotificationService,
                         AlertTermRepository alertTermRepository,
                         WAContactService waContactService,
                         WAMessageService waMessageService,
                         WAGroupService waGroupService,
                         br.com.modware.transrv.repository.EmployeeWAContactRepository employeeWAContactRepository,
                         TicketWebSocketService ticketWebSocketService,
                         br.com.modware.transrv.repository.EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.employeeAlertTermRepository = employeeAlertTermRepository;

        this.instanceEvolutionService = instanceEvolutionService;
        this.scheduler = scheduler;
        this.ticketNotificationService = ticketNotificationService;
        this.alertTermRepository = alertTermRepository;
        this.waContactService = waContactService;
        this.waMessageService = waMessageService;
        this.waGroupService = waGroupService;
        this.employeeWAContactRepository = employeeWAContactRepository;
        this.ticketWebSocketService = ticketWebSocketService;
        this.employeeRepository = employeeRepository;
    }


    public Ticket openTicket(WAMessage waMessage, AlertTerm alertTerm, WAContact waContact, WAGroup waGroup, Employee employee) throws SchedulerException {
        if (waMessage == null || alertTerm == null || waContact == null || waGroup == null) {
            throw new IllegalArgumentException("Invalid parameters for opening a ticket");
        }


        if (ticketRepository.existsByMessageResponsibleForOpeningTheCall(waMessage)) {
            throw new IllegalStateException("Já existe ticket aberto para essa mensagem");
        }


        Ticket ticket = new Ticket();
        ticket.setAlertTerm(alertTerm);
        ticket.setMessageResponsibleForOpeningTheCall(waMessage);
        ticket.setCreatedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        ticket.setStatus(Ticket.Status.OPEN);
        ticket.setWaGroup(waGroup);
        ticket.setCurrentEscalationLevel(1);
        ticket.setContactResponsibleForOpeningTheCall(waContact);
        if (employee != null) {
            ticket.setEmployeeResponsibleForOpeningTheCall(employee);
        }

        ticket = ticketRepository.save(ticket);

        notifyEmployees(ticket, 1);

        scheduleEscalation(ticket, 2, Duration.ofSeconds(escalationLevel2DelaySeconds));
        scheduleEscalation(ticket, 3, Duration.ofSeconds(escalationLevel3DelaySeconds));
        scheduleExpiration(ticket, Duration.ofSeconds(ticketExpirationSeconds));

   
        
        // Publica evento via WebSocket para todos os clientes conectados
        ticketWebSocketService.publishTicketOpened(ticket);
        
        return ticket;
    }

    public Ticket openTestTicket() throws SchedulerException {
        AlertTerm alertTerm = alertTermRepository.findById(106L)
                .orElseThrow(() -> new RuntimeException("AlertTerm com ID 106 não encontrado."));

        // Criar entidades mock para WAMessage, WAContact e WAGroup
        WAContact waContact = new WAContact();
        waContact.setPhoneNumber("5511999999999");
        waContact.setName("Teste Contato");
        waContact = waContactService.save(waContact);

        WAGroup waGroup = new WAGroup();
        waGroup.setEvolutionGroupId("1234567890@g.us");
        waGroup.setGroupName("Grupo de Teste");
        waGroup.setMonitored(true);
        waGroup = waGroupService.save(waGroup);

        WAMessage waMessage = new WAMessage();
        waMessage.setSender(waContact);
        waMessage.setWaGroup(waGroup);
        waMessage.setFromMe(false);
        waMessage.setMessageContent("Mensagem de teste para abertura de chamado");
        waMessage.setSentAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        waMessage = waMessageService.save(waMessage);

        // Abrir o ticket usando o método existente
        return openTicket(waMessage, alertTerm, waContact, waGroup, null);
    }


    @org.springframework.transaction.annotation.Transactional
    private void notifyEmployees(Ticket ticket, int level) {

        List<Employee> employees = employeeAlertTermRepository
                .findByAlertTermAndPriorityLevel(ticket.getAlertTerm(), level)
                .stream()
                .map(EmployeeAlertTerm::getEmployee)
                .toList();
        
        Ticket savedTicket = ticketRepository.save(ticket);

        // Agrupar por telefone via tabela de ligação EmployeeWAContact
        java.util.List<br.com.modware.transrv.model.EmployeeWAContact> links = employeeWAContactRepository.findWithContactByEmployeeIn(employees);

        Map<String, List<Employee>> employeesByPhone = links.stream()
                .filter(l -> l.getWaContact() != null && l.getWaContact().getPhoneNumber() != null)
                .collect(Collectors.groupingBy(l -> l.getWaContact().getPhoneNumber(),
                        Collectors.mapping(br.com.modware.transrv.model.EmployeeWAContact::getEmployee, Collectors.toList())));

        for (Map.Entry<String, List<Employee>> entry : employeesByPhone.entrySet()) {
            String phone = entry.getKey();
            List<Employee> groupedEmployees = entry.getValue();

            String employeeNames = groupedEmployees.stream()
                    .map(e -> "*" + e.getName() + "*")
                    .collect(Collectors.joining(", "));

            boolean sent = instanceEvolutionService.sendMessageToPhone(
                    phone, employeeNames, savedTicket, level, ticket.getWaGroup()
            );

            if (sent) {
                org.slf4j.LoggerFactory.getLogger(TicketService.class).info(
                        "Funcionários alertados | ticket={} | nível={} | telefonesDestinatários={} | nomes={}",
                        savedTicket.getId(), level, phone, employeeNames);
                for (Employee e : groupedEmployees) {
                    TicketNotification notif = new TicketNotification();
                    notif.setTicket(savedTicket);
                    notif.setEmployee(e);
                    notif.setEscalationLevel(level);
                    notif.setNotifiedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
                    ticketNotificationService.save(notif);
                }
            } else {
                org.slf4j.LoggerFactory.getLogger(TicketService.class).warn(
                        "Falha ao alertar funcionários | ticket={} | nível={} | telefone={}",
                        savedTicket.getId(), level, phone);
            }
        }

        ticket.setCurrentEscalationLevel(level);
        ticketRepository.save(ticket);
    }




    private void scheduleEscalation(Ticket ticket, int nextLevel, Duration delay) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(EscalationJob.class)
                .withIdentity(ticket.getId() + "-level-"+ nextLevel)
                .usingJobData("ticketId", ticket.getId())
                .usingJobData("level", nextLevel)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(Instant.now().plus(delay)))
                .withSchedule(org.quartz.SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)
                        .withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);

    }

    private void scheduleExpiration(Ticket ticket, Duration delay) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(ExpireTicketJob.class)
                .withIdentity(ticket.getId() + "-expire")
                .usingJobData("ticketId", ticket.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(Instant.now().plus(delay)))
                .withSchedule(org.quartz.SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(0)
                        .withMisfireHandlingInstructionFireNow())
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    public void escalate(Long ticketId, int level) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() != Ticket.Status.OPEN) return;

        notifyEmployees(ticket, level);
        
        ticketWebSocketService.publishTicketEscalated(ticket);
        
    }

    public void expire(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setStatus(Ticket.Status.CLOSED_WITHOUT_SOLUTION);
            ticket.setClosedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
            ticket = ticketRepository.save(ticket);            
            // Publica evento de fechamento via WebSocket
            ticketWebSocketService.publishTicketClosed(ticket);
            try {
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-expire"));
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-2"));
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-3"));
            } catch (SchedulerException e) {
                throw new RuntimeException("Erro ao cancelar agendamentos do ticket " + ticket.getId(), e);
            }
        }
    }

    public Ticket closeByDashboard(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setStatus(Ticket.Status.CLOSED);
            ticket.setClosedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
            ticket = ticketRepository.save(ticket);
            try {
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-expire"));
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-2"));
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-3"));
            } catch (SchedulerException e) {
                throw new RuntimeException("Erro ao cancelar agendamentos do ticket " + ticket.getId(), e);
            }
            
            // Publica evento de fechamento via WebSocket
            ticketWebSocketService.publishTicketClosed(ticket);
        }
        return ticket;
    }

    public Ticket closeAsIncorrect(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setOpenedIncorrectly(true);
            ticket.setStatus(Ticket.Status.CLOSED);
            ticket.setClosedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
            ticket = ticketRepository.save(ticket);
            try {
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-expire"));
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-2"));
                scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-3"));
             
            } catch (SchedulerException e) {
                throw new RuntimeException("Erro ao cancelar agendamentos do ticket " + ticket.getId(), e);
            }
            
            ticketWebSocketService.publishTicketMarkedIncorrect(ticket);
        }
        return ticket;
    }

   
    public Ticket closeTicketWithEmployee(Long ticketId, Long employeeId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() != Ticket.Status.OPEN) {
            throw new IllegalStateException("Ticket não está aberto. Status atual: " + ticket.getStatus());
        }

        ticket.setStatus(Ticket.Status.CLOSED);
        ticket.setClosedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

       
        if (employeeId != null) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId));
            ticket.setEmployeeResponsibleForClosingTheCall(employee);
            log.info("Ticket {} fechado com funcionário responsável: {} (ID: {})", 
                    ticket.getId(), employee.getName(), employee.getId());
        }

        ticket = ticketRepository.save(ticket);

        try {
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-expire"));
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-2"));
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-3"));
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao cancelar agendamentos do ticket " + ticket.getId(), e);
        }
        
        ticketWebSocketService.publishTicketClosed(ticket);

        return ticket;
    }



    public boolean tryCloseTicket(String stanzaId, String messageContent, WAContact closingContact, WAMessage closingMessage) {
        boolean closed = false;

        String contentPreview = messageContent != null
                ? (messageContent.length() > 200 ? messageContent.substring(0, 200) + "..." : messageContent)
                : null;
        log.info("tryCloseTicket chamado | stanzaId={} | contact={}({}) | content={}",
                stanzaId,
                closingContact != null ? closingContact.getName() : null,
                closingContact != null ? closingContact.getPhoneNumber() : null,
                contentPreview);

        if (messageContent != null) {
            Pattern phrasePattern = Pattern.compile(
                    "(?i)(?:ticket|chamado)\\s*finalizad[oa][\\s,:-]*.*?\\bid\\b\\s*[:\\-]?\\s*(\\d+)");
            java.util.regex.Matcher phraseMatcher = phrasePattern.matcher(messageContent);

            if (phraseMatcher.find()) {
                Long ticketId = Long.valueOf(phraseMatcher.group(1));
                log.info("Comando de fechamento detectado | ticketId={} | origem=privado?={} | origem=grupo?={}",
                        ticketId,
                        closingMessage != null && closingMessage.getWaGroup() == null,
                        closingMessage != null && closingMessage.getWaGroup() != null);

                Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
                if (ticketOpt.isEmpty()) {
                    log.warn("Ticket não encontrado | ticketId={}", ticketId);
                } else {
                    Ticket ticket = ticketOpt.get();
                    if (ticket.getStatus() == Ticket.Status.OPEN) {
                        log.info("Ticket encontrado e OPEN, fechando | ticketId={}", ticket.getId());
                        closeTicket(ticket, closingContact, closingMessage);
                        closed = true;
                    } else {
                        log.info("Ticket encontrado porém status != OPEN | ticketId={} | status={}", ticket.getId(), ticket.getStatus());
                    }
                }
            } else {
                log.debug("Nenhum padrão de fechamento detectado na mensagem");
            }
        } else {
            log.debug("Mensagem nula recebida em tryCloseTicket");
        }

        log.info("tryCloseTicket finalizado | closed={}", closed);
        return closed;
    }


    private void closeTicket(Ticket ticket, WAContact closingContact, WAMessage closingMessage) {
        try {
            ticket.setStatus(Ticket.Status.CLOSED);
            ticket.setClosedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
            ticket.setContactResponsibleForClosingTheCall(closingContact);
            ticket.setMessageResponsibleForClosingTheCall(closingMessage);
            if (closingMessage != null && closingMessage.getEmployee() != null) {
                ticket.setEmployeeResponsibleForClosingTheCall(closingMessage.getEmployee());
            }
            ticket = ticketRepository.save(ticket);
            
      
            ticketWebSocketService.publishTicketClosed(ticket);


            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-expire"));
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-2"));
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-3"));

        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao cancelar agendamentos do ticket " + ticket.getId(), e);
        }
    }

    public void sendGroupAlertIfOpenLevel3(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) return;
        if (ticket.getStatus() != Ticket.Status.OPEN) return;
        if (ticket.getCurrentEscalationLevel() < 3) return;

        boolean sent = instanceEvolutionService.sendMessageToGroup(ticket.getWaGroup(), ticket, 3);
        org.slf4j.LoggerFactory.getLogger(TicketService.class).info(
                "Alerta em loop enviado ao grupo | ticket={} | grupoId={} | sucesso={}",
                ticket.getId(),
                ticket.getWaGroup() != null ? ticket.getWaGroup().getEvolutionGroupId() : null,
                sent
        );
    }
}
