package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "agent")
@Getter
public class Agent {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt;

}
