package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String name;

    @OneToOne
    @JoinColumn(name = "wa_contact_id")
    @Getter
    private WAContact waContact;

    @Getter
    private LocalTime enterTime;
    @Getter
    private LocalTime exitTime;

    private Integer priorityLevel;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeAlertTerm> employeeAlertTerms = new HashSet<>();

}