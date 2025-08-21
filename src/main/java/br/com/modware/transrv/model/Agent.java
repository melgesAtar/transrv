package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;


@Entity
@Table(name = "agent")
public class Agent {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "prompt", columnDefinition = "TEXT")
    @Getter
    private String prompt;


}
