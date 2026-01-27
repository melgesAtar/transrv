package br.com.modware.transrv.dto.user;

import br.com.modware.transrv.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para criação de novo usuário")
public record UserCreateRequest(
    @Schema(description = "Nome de usuário único", example = "novoUsuario", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,
    @Schema(description = "Senha do usuário", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
    String password,
    @Schema(description = "Papel do usuário (ADMIN ou USER)", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    Role role
) {
    
}
