package br.com.modware.transrv.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com informações do usuário")
public record AuthResponse(
    @Schema(description = "Nome de usuário", example = "admin")
    String username,
    @Schema(description = "Papel do usuário (ADMIN ou USER)", example = "ADMIN")
    String role
) {}