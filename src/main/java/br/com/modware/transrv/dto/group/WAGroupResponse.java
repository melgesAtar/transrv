package br.com.modware.transrv.dto.group;

import br.com.modware.transrv.model.WAGroup;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um grupo de WhatsApp")
public record WAGroupResponse(
    @Schema(description = "ID do grupo", example = "1")
    Long id,
    @Schema(description = "ID do grupo no Evolution API", example = "1234567890@g.us")
    String evolutionGroupId,
    @Schema(description = "Nome do grupo", example = "Grupo de Suporte")
    String groupName,
    @Schema(description = "ID do agente associado (pode ser null)", example = "1")
    Long agentId,
    @Schema(description = "Nome do agente associado (pode ser null)", example = "Agente de Suporte")
    String agentName,
    @Schema(description = "Indica se o grupo está sendo monitorado", example = "true")
    Boolean isMonitored
) {
    public static WAGroupResponse from(WAGroup group) {
        return new WAGroupResponse(
            group.getId(),
            group.getEvolutionGroupId(),
            group.getGroupName(),
            group.getAgent() != null ? group.getAgent().getId() : null,
            group.getAgent() != null ? group.getAgent().getName() : null,
            group.isMonitored()
        );
    }
}
