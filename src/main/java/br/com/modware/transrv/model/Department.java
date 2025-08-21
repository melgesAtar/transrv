package br.com.modware.transrv.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Department {
    @Id
    private Long id;
    private String name;

}
