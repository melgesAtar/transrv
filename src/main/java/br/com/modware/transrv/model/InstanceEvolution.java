package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "instance_evolution")
@Data
public class InstanceEvolution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String instanceName;

}