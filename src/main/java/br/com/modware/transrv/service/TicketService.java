package br.com.modware.transrv.service;

import br.com.modware.transrv.model.*;
import br.com.modware.transrv.quartz.EscalationJob;
import br.com.modware.transrv.quartz.ExpireTicketJob;
import br.com.modware.transrv.repository.EmployeeRepository;
import br.com.modware.transrv.repository.TicketRepository;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;
    private final InstanceEvolutionService instanceEvolutionService;
    private final EmployeeService employeeService;
    private final Scheduler scheduler;
    private final TicketNotificationService ticketNotificationService;

    public TicketService(TicketRepository ticketRepository,
                         EmployeeRepository employeeRepository,
                         InstanceEvolutionService instanceEvolutionService,
                         EmployeeService employeeService,
                         Scheduler scheduler,
                         TicketNotificationService ticketNotificationService) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
        this.instanceEvolutionService = instanceEvolutionService;
        this.employeeService = employeeService;
        this.scheduler = scheduler;
        this.ticketNotificationService = ticketNotificationService;
    }


    public Ticket openTicket(WAMessage waMessage, AlertTerm alertTerm, WAContact waContact, WAGroup waGroup) throws SchedulerException {
        if (waMessage == null || alertTerm == null || waContact == null || waGroup == null) {
            throw new IllegalArgumentException("Invalid parameters for opening a ticket");
        }


        if (ticketRepository.existsByMessageResponsibleForOpeningTheCall(waMessage)) {
            throw new IllegalStateException("Já existe ticket aberto para essa mensagem");
        }

        contactHasOpenTicketInTheGroupByTheSameReason(waContact, waGroup, alertTerm);

        Ticket ticket = new Ticket();
        ticket.setAlertTerm(alertTerm);
        ticket.setMessageResponsibleForOpeningTheCall(waMessage);
        ticket.setCreatedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        ticket.setStatus(Ticket.Status.OPEN);
        ticket.setWaGroup(waGroup);
        ticket.setCurrentEscalationLevel(1);
        ticket.setContactResponsibleForOpeningTheCall(waContact);

        ticket = ticketRepository.save(ticket);

        notifyEmployees(ticket, 1);

        scheduleEscalation(ticket, 2, Duration.ofMinutes(1));
        scheduleEscalation(ticket, 3, Duration.ofMinutes(2));
        scheduleExpiration(ticket, Duration.ofHours(1));

        return ticket;
    }


    private void notifyEmployees(Ticket ticket, int level) {
        List<Employee> employees = employeeRepository
                .findByPriorityLevelAndAlertTermsContains(level, ticket.getAlertTerm());

        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));

        List<Employee> availables = employees.stream()
                .filter(e -> employeeService.isEmployeeAvailable(e, now))
                .toList();


        if (availables.isEmpty()) {

            employees.stream()
                    .min(Comparator.comparing(Employee::getEnterTime))
                    .ifPresent(next -> {
                        LocalDateTime nextTime = now.with(next.getEnterTime());
                        if (nextTime.isBefore(now)) {
                            nextTime = nextTime.plusDays(1);
                        }
                        scheduleNotifyAt(ticket, level, nextTime);
                    });
            return;
        }


        Ticket savedTicket = ticketRepository.save(ticket);

        for (Employee e : employees) {
            instanceEvolutionService.sendMessageToEmployee(savedTicket.getWaGroup(), e, savedTicket, level);

            TicketNotification notif = new TicketNotification();
            notif.setTicket(savedTicket);
            notif.setEmployee(e);
            notif.setEscalationLevel(level);
            notif.setNotifiedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

            ticketNotificationService.save(notif);
        }


        ticket.setCurrentEscalationLevel(level);
        ticketRepository.save(ticket);
    }


    public void contactHasOpenTicketInTheGroupByTheSameReason(WAContact waContact, WAGroup waGroup, AlertTerm alertTerm) {
        if (waContact == null || waGroup == null || alertTerm == null) {
            throw new IllegalArgumentException("Invalid parameters for checking open ticket");
        }

        boolean hasOpenTicket = ticketRepository.existsByContactResponsibleForOpeningTheCallAndWaGroupAndAlertTermAndStatus(
               waContact , waGroup, alertTerm, Ticket.Status.OPEN
        );

        if (hasOpenTicket) {
            throw new IllegalStateException("Contact already has an open ticket for this reason in the group.");
        }
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
    }

    public void expire(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        if (ticket.getStatus() == Ticket.Status.OPEN) {
            ticket.setStatus(Ticket.Status.CLOSED_WITHOUT_SOLUTION);
            ticket.setClosedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
    }

    public void resolveTicket(Long ticketId, Employee resolver, WAMessage closingMessage) throws SchedulerException {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setStatus(Ticket.Status.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        ticketRepository.save(ticket);


        scheduler.deleteJob(JobKey.jobKey(ticketId + "-expire"));
        scheduler.deleteJob(JobKey.jobKey(ticketId + "-level-2"));
        scheduler.deleteJob(JobKey.jobKey(ticketId + "-level-3"));
    }


    private void scheduleNotifyAt(Ticket ticket, int level, LocalDateTime when) {
        try {
            JobDetail job = JobBuilder.newJob(EscalationJob.class)
                    .withIdentity("ticket_" + ticket.getId() + "_level_" + level, "tickets")
                    .usingJobData("ticketId", ticket.getId())
                    .usingJobData("level", level)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .startAt(Date.from(when.atZone(ZoneId.systemDefault()).toInstant()))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao agendar notificação para horário de expediente", e);
        }
    }

    public boolean tryCloseTicket(String stanzaId, String messageContent, WAContact closingContact, WAMessage closingMessage) {
        boolean closed = false;

        if (stanzaId != null && !stanzaId.isBlank()) {
            ticketRepository.findByMessageResponsibleForOpeningTheCall_EvolutionMessageId(stanzaId)
                    .ifPresent(ticket -> {
                        if (ticket.getStatus() == Ticket.Status.OPEN) {
                            closeTicket(ticket, closingContact, closingMessage);
                        }
                    });
            closed = true;
        }

        if (messageContent != null) {
            Pattern pattern = Pattern.compile("ID[: ](\\d+)", Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(messageContent);
            if (matcher.find()) {
                Long ticketId = Long.valueOf(matcher.group(1));
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

        } catch (SchedulerException e) {
            throw new RuntimeException("Erro ao cancelar agendamentos do ticket " + ticket.getId(), e);
        }
    }
}
