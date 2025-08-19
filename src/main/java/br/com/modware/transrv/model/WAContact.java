package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "wa_contact")
@Data
public class WAContact {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phoneNumber;
    @OneToMany(mappedBy = "sender" ,
            cascade = CascadeType.ALL,orphanRemoval = true)
    private List<WAMessage> messages;
}
