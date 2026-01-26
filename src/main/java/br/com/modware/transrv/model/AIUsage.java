package br.com.modware.transrv.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage")
public class AIUsage {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private LocalDateTime createdAt;
    @OneToOne
    @JoinColumn(name = "message_id", unique = true)
    @Getter
    private WAMessage message;

    public AIUsage(int promptTokens, int completionTokens, int totalTokens, LocalDateTime createdAt,
            WAMessage message) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.createdAt = createdAt;
        this.message = message;
    }
}
