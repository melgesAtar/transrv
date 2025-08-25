package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "wa_contact_id")
    @Getter
    private WAContact waContact;

    private LocalTime enterTime;

    private LocalTime exitTime;

    private Integer priorityLevel;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToMany
    @JoinTable(
            name = "employee_alert_term",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "alert_term_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id","alert_term_id"})
    )
    private Set<AlertTerm> alertTerms = new HashSet<>();

}