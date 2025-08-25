package br.com.modware.transrv.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_notification")
public class TicketNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Ticket ticket;

    @ManyToOne
    private Employee employee;

    private int escalationLevel;

    private LocalDateTime notifiedAt;

}
