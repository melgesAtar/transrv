package br.com.modware.transrv.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para alteração de senha")
public record ChangePasswordRequest(
    @Schema(description = "Senha atual do usuário", example = "senhaAntiga123", requiredMode = Schema.RequiredMode.REQUIRED)
    String oldPassword,
    @Schema(description = "Nova senha do usuário", example = "novaSenha456", requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword
) {
    
}
