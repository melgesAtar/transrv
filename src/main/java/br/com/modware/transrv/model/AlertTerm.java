package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "alert_term")
public class AlertTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=64)
    @Getter
    private String code;        // imutável (ex.: RISCO_ETA_ORIGEM)

    @Column(nullable=false, length=120)
    private String name;        // label editável (ex.: "Risco ETA Origem")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertTerm.Status status = AlertTerm.Status.ACTIVE;


    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;



    public enum Status { ACTIVE, INACTIVE }
}

