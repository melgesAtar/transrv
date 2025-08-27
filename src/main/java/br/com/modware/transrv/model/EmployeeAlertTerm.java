package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "employee_alert_term")
public class EmployeeAlertTerm {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alert_term_id", nullable = false)
    private AlertTerm alertTerm;

    @Column(nullable = false)
    private Integer priorityLevel;

}
