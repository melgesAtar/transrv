package br.com.modware.transrv.service;

import br.com.modware.transrv.model.*;
import br.com.modware.transrv.repository.EmployeeRepository;
import br.com.modware.transrv.repository.TicketRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;



    public TicketService(TicketRepository ticketRepository, EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
    }

    public Ticket openTicket(WAMessage waMessage, AlertTerm alertTerm, WAContact waContact, WAGroup waGroup) {
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

        notifyEmployees(waMessage, ticket, 1);

        scheduleEscalation(ticket, 2, Duration.ofMinutes(30)); // exemplo
        scheduleEscalation(ticket, 3, Duration.ofMinutes(60));
        scheduleExpiration(ticket, Duration.ofHours(24));

        return ticketRepository.save(ticket);
    }

    private void notifyEmployees(WAMessage waMessge, Ticket ticket, int level) {
        List<Employee> employees = employeeRepository
                .findByPriorityLevelAndAlertTermsContains(level, ticket.getAlertTerm());

        for (Employee e : employees) {
            groupService.sendMessageToEmployee(ticket.getWaGroup(), e, ticket);
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

}
