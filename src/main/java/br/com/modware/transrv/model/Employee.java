package br.com.modware.transrv.model;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    private WAContact waContact;
    private LocalTime enterTime;
    private LocalTime exitTime;
    private Integer priorityLevel;
    @ManyToOne
    private Department department;
}
