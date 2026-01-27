package br.com.modware.transrv.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de login do usuário")
public record LoginRequest(
    @Schema(description = "Nome de usuário", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,
    @Schema(description = "Senha do usuário", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
    String password
) {}

