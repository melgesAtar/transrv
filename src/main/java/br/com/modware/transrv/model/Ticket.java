package br.com.modware.transrv.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.time.LocalDateTime;

public class Ticket {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private String id;
    private AlertTerm alertTerm;
    @OneToOne
    private Employee employeeResponsibleForOpeningTheCall;
    @OneToOne
    private Employee employeeResponsibleForClosingTheCall;
    @OneToOne
    private WAMessage messageResponsibleForOpeningTheCall;
    @OneToOne
    private WAMessage messageResponsibleForClosingTheCall;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private Status status;

    public enum Status {
        OPEN,
        CLOSED,
        CLOSED_WITHOUT_SOLUTION,
    }
}
