package br.com.modware.transrv.service;

import br.com.modware.transrv.model.*;
import br.com.modware.transrv.quartz.EscalationJob;
import br.com.modware.transrv.quartz.ExpireTicketJob;
import br.com.modware.transrv.repository.EmployeeAlertTermRepository;
import br.com.modware.transrv.repository.TicketRepository;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeAlertTermRepository employeeAlertTermRepository;
    private final InstanceEvolutionService instanceEvolutionService;
    private final Scheduler scheduler;
    private final TicketNotificationService ticketNotificationService;

    private final DashboardBroadcaster dashboardBroadcaster;

    public TicketService(TicketRepository ticketRepository,
                          EmployeeAlertTermRepository employeeAlertTermRepository,
                         InstanceEvolutionService instanceEvolutionService,
                         Scheduler scheduler,
                         TicketNotificationService ticketNotificationService,
                         DashboardBroadcaster dashboardBroadcaster) {
        this.ticketRepository = ticketRepository;
        this.employeeAlertTermRepository = employeeAlertTermRepository;

        this.instanceEvolutionService = instanceEvolutionService;
        this.scheduler = scheduler;
        this.ticketNotificationService = ticketNotificationService;
        this.dashboardBroadcaster = dashboardBroadcaster;
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
        dashboardBroadcaster.publishUpdate();

        scheduleEscalation(ticket, 2, Duration.ofMinutes(1));
        scheduleEscalation(ticket, 3, Duration.ofMinutes(2));
        scheduleExpiration(ticket, Duration.ofHours(1));

        return ticket;
    }


    private void notifyEmployees(Ticket ticket, int level) {

        List<Employee> employees = employeeAlertTermRepository
                .findByAlertTermAndPriorityLevel(ticket.getAlertTerm(), level)
                .stream()
                .map(EmployeeAlertTerm::getEmployee)
                .toList();

        Ticket savedTicket = ticketRepository.save(ticket);

        Map<String, List<Employee>> employeesByPhone = employees.stream()
                .collect(Collectors.groupingBy(e -> e.getWaContact().getPhoneNumber()));

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
                .build();

        scheduler.scheduleJob(job, trigger);
        dashboardBroadcaster.publishUpdate();
    }

    private void scheduleExpiration(Ticket ticket, Duration delay) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(ExpireTicketJob.class)
                .withIdentity(ticket.getId() + "-expire")
                .usingJobData("ticketId", ticket.getId())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Date.from(Instant.now().plus(delay)))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    public void escalate(Long ticketId, int level) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() != Ticket.Status.OPEN) return;

        notifyEmployees(ticket, level);
        dashboardBroadcaster.publishUpdate();

        // Se chegou ao nível 3, iniciar alerta em loop no grupo
        if (level == 3) {
            scheduleGroupLoopAlert(ticket);
        }
    }

    public void expire(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setStatus(Ticket.Status.CLOSED_WITHOUT_SOLUTION);
            ticket.setClosedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
            // Atualiza dashboard em tempo real
            dashboardBroadcaster.publishUpdate();
        }
    }

    public Ticket closeByDashboard(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setStatus(Ticket.Status.CLOSED);
            ticket.setClosedAt(LocalDateTime.now());
            ticket = ticketRepository.save(ticket);
            dashboardBroadcaster.publishUpdate();
        }
        return ticket;
    }




    // método de agendamento por horário removido conforme nova regra de notificação imediata

    public boolean tryCloseTicket(String stanzaId, String messageContent, WAContact closingContact, WAMessage closingMessage) {
        boolean closed = false;

        // Novo critério de fechamento: procurar frases do tipo
        // "ticket finalizado id 123" (variações de caixa e pontuação)
        // Não depende de ser resposta a uma mensagem específica.
        if (messageContent != null) {
            Pattern phrasePattern = Pattern.compile(
                    "(?i)(?:ticket|chamado)\\s*finalizad[oa][\\s,:-]*.*?\\bid\\b\\s*[:\\-]?\\s*(\\d+)");
            java.util.regex.Matcher phraseMatcher = phrasePattern.matcher(messageContent);

            if (phraseMatcher.find()) {
                Long ticketId = Long.valueOf(phraseMatcher.group(1));
                ticketRepository.findById(ticketId).ifPresent(ticket -> {
                    if (ticket.getStatus() == Ticket.Status.OPEN) {
                        closeTicket(ticket, closingContact, closingMessage);
                    }
                });
                closed = true;
            }
        }


        return closed;
    }


    private void closeTicket(Ticket ticket, WAContact closingContact, WAMessage closingMessage) {
        try {
            ticket.setStatus(Ticket.Status.CLOSED);
            ticket.setClosedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
            ticket.setContactResponsibleForClosingTheCall(closingContact);
            ticket.setMessageResponsibleForClosingTheCall(closingMessage);
            ticketRepository.save(ticket);


            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-expire"));
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-2"));
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-level-3"));
            scheduler.deleteJob(JobKey.jobKey(ticket.getId() + "-group-loop"));

            // Atualiza dashboard em tempo real
            dashboardBroadcaster.publishUpdate();
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao cancelar agendamentos do ticket " + ticket.getId(), e);
        }
    }

    private void scheduleGroupLoopAlert(Ticket ticket) {
        try {
            JobDetail job = JobBuilder.newJob(br.com.modware.transrv.quartz.GroupAlertLoopJob.class)
                    .withIdentity(ticket.getId() + "-group-loop")
                    .usingJobData("ticketId", ticket.getId())
                    .build();

            // Disparo inicial após 10 minutos (carência para o nível 3 responder), depois a cada 3 minutos
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(org.quartz.SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(3)
                            .repeatForever())
                    .startAt(Date.from(Instant.now().plus(Duration.ofMinutes(1))))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar alerta em loop para o ticket " + ticket.getId(), e);
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
