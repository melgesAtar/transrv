package br.com.modware.transrv.dto.agent;

import br.com.modware.transrv.model.Agent;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um agente")
public record AgentResponse(
    @Schema(description = "ID do agente", example = "1")
    Long id,
    @Schema(description = "Nome do agente", example = "Agente de Suporte")
    String name,
    @Schema(description = "Prompt do agente (pode ser null)", example = "Você é um assistente...")
    String prompt
) {
    public static AgentResponse from(Agent agent) {
        return new AgentResponse(
            agent.getId(),
            agent.getName(),
            agent.getPrompt()
        );
    }
}
