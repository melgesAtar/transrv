package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;
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

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeAlertTerm> employeeAlertTerms = new HashSet<>();

    @Column(name = "isActive")
    private boolean isActive;

}