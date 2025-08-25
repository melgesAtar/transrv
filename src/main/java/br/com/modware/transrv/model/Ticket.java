package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private Integer currentEscalationLevel = 1;

    @ManyToOne
    @JoinColumn(name = "alert_term_id", nullable = false)
    private AlertTerm alertTerm;

    @ManyToOne
    @JoinColumn(name = "contact_open_id")
    private WAContact contactResponsibleForOpeningTheCall;

    @ManyToOne
    @JoinColumn(name = "contact_close_id")
    private WAContact contactResponsibleForClosingTheCall;

    @OneToOne
    private WAMessage messageResponsibleForOpeningTheCall;

    @OneToOne
    private WAMessage messageResponsibleForClosingTheCall;

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private Status status;

    @ManyToOne
    private WAGroup waGroup;


    public enum Status {
        OPEN,
        CLOSED,
        CLOSED_WITHOUT_SOLUTION,
    }

    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", currentEscalationLevel=" + currentEscalationLevel +
                ", status=" + status +
                '}';
    }
}
