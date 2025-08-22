package br.com.modware.transrv.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private String id;
    private Integer currentEscalationLevel = 1;
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

    @OneToOne(optional = true)
    private WAGroup waGroup;


    public enum Status {
        OPEN,
        CLOSED,
        CLOSED_WITHOUT_SOLUTION,
    }
}
