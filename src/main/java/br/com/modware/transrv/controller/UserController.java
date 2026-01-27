package br.com.modware.transrv.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import br.com.modware.transrv.service.UserService;
import br.com.modware.transrv.dto.user.UserCreateRequest;
import br.com.modware.transrv.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários do sistema")
@SecurityRequirement(name = "JWT")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Criar novo usuário",
        description = "Cria um novo usuário no sistema. Apenas administradores podem criar usuários."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão (apenas ADMIN)")
    })
    @PostMapping
    public User createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do novo usuário")
            @RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }
}
