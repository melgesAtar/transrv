package br.com.modware.transrv.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

@Entity
@Table(name = "wa_group")
@Data
public class WAGroup {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String evolutionGroupId;
    @Getter
    private String groupName;
    @OneToOne
    private WAConversation WAConversation;
    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = true)
    private Agent agent;
    private boolean isMonitored;


}
