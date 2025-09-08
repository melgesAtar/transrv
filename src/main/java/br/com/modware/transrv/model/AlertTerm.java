package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


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
    private String name;     
    
    @Column(nullable=false, length=120)
    @Getter
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertTerm.Status status = AlertTerm.Status.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "alertTerm", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeAlertTerm> employeeAlertTerms = new HashSet<>();


    public enum Status { ACTIVE, INACTIVE }
}

