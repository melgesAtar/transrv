package br.com.modware.transrv.service;

import br.com.modware.transrv.model.*;
import br.com.modware.transrv.quartz.EscalationJob;
import br.com.modware.transrv.quartz.ExpireTicketJob;
import br.com.modware.transrv.repository.EmployeeRepository;
import br.com.modware.transrv.repository.TicketRepository;
import org.quartz.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;
    private final InstanceEvolutionService instanceEvolutionService;
    private final Scheduler scheduler;


    public TicketService(TicketRepository ticketRepository, EmployeeRepository employeeRepository, InstanceEvolutionService instanceEvolutionService, Scheduler scheduler) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
        this.instanceEvolutionService = instanceEvolutionService;
        this.scheduler = scheduler;
    }

    public Ticket openTicket(WAMessage waMessage, AlertTerm alertTerm, WAContact waContact, WAGroup waGroup) throws SchedulerException {
        if(waMessage == null || alertTerm == null || waContact == null || waGroup == null) {
            throw new IllegalArgumentException("Invalid parameters for opening a ticket");
        }
        contactHasOpenTicketInTheGroupByTheSameReason(waContact, waGroup, alertTerm);

        Ticket ticket = new Ticket();
        ticket.setAlertTerm(alertTerm);
        ticket.setMessageResponsibleForOpeningTheCall(waMessage);
        ticket.setCreatedAt(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));
        ticket.setStatus(Ticket.Status.OPEN);
        ticket.setWaGroup(waGroup);
        ticket.setCurrentEscalationLevel(1);

        notifyEmployees(ticket, 1);

        scheduleEscalation(ticket, 2, Duration.ofMinutes(30));
        scheduleEscalation(ticket, 3, Duration.ofMinutes(60));
        scheduleExpiration(ticket, Duration.ofHours(24));

        return ticketRepository.save(ticket);
    }

    private void notifyEmployees(Ticket ticket, int level) {
        List<Employee> employees = employeeRepository
                .findByPriorityLevelAndAlertTermsContains(level, ticket.getAlertTerm());

        for (Employee e : employees) {
            instanceEvolutionService.sendMessageToEmployee(ticket.getWaGroup(), e, ticket , level);
        }

        ticket.setCurrentEscalationLevel(level);
        ticketRepository.save(ticket);
    }


    public void contactHasOpenTicketInTheGroupByTheSameReason(WAContact waContact, WAGroup waGroup, AlertTerm alertTerm) {
        if (waContact == null || waGroup == null || alertTerm == null) {
            throw new IllegalArgumentException("Invalid parameters for checking open ticket");
        }

        boolean hasOpenTicket = ticketRepository.existsByContactAndWaGroupAndAlertTermAndStatus(
                waContact, waGroup, alertTerm, Ticket.Status.OPEN
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
        ticket.setEmployeeResponsibleForClosingTheCall(resolver);
        ticket.setMessageResponsibleForClosingTheCall(closingMessage);
        ticketRepository.save(ticket);


        scheduler.deleteJob(JobKey.jobKey(ticketId + "-expire"));
        scheduler.deleteJob(JobKey.jobKey(ticketId + "-level-2"));
        scheduler.deleteJob(JobKey.jobKey(ticketId + "-level-3"));
    }



}
