package br.com.modware.transrv.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Department {
    @Id
    private Long id;
    private String name;

}
