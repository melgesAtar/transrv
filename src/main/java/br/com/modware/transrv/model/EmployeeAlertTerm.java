package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "employee_alert_term",
       uniqueConstraints = @UniqueConstraint(name = "uk_emp_alert_priority",
               columnNames = {"employee_id", "alert_term_id", "priorityLevel"}))
public class EmployeeAlertTerm {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alert_term_id", nullable = false)
    @Getter
    @Setter
    private AlertTerm alertTerm;

    @Column(name = "priorityLevel", nullable = false)
    @Getter
    @Setter
    private Integer priorityLevel;

}
