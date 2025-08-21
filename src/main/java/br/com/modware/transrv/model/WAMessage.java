package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Entity
@Table(name = "wa_message")
@Data
public class WAMessage {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long id;
    private String evolutionMessageId;
    @Getter
    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;
    @ManyToOne
    private WAConversation waConversation;
    @ManyToOne
    private WAContact sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "alert_term_id")
    private AlertTerm alertTerm;


    private LocalDateTime sentAt;

}
