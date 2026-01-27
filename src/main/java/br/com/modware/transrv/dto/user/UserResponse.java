package br.com.modware.transrv.dto.user;

import br.com.modware.transrv.model.Role;
import br.com.modware.transrv.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um usuário")
public record UserResponse(
    @Schema(description = "ID do usuário", example = "1")
    Long id,
    @Schema(description = "Nome de usuário", example = "admin")
    String username,
    @Schema(description = "Papel do usuário (ADMIN ou USER)", example = "ADMIN")
    Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole()
        );
    }
}