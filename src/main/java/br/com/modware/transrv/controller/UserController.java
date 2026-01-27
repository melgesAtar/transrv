package br.com.modware.transrv.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import br.com.modware.transrv.service.UserService;
import br.com.modware.transrv.service.UserQueryService;
import br.com.modware.transrv.dto.user.UserCreateRequest;
import br.com.modware.transrv.dto.user.UserPageResponse;
import br.com.modware.transrv.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;


@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários do sistema")
@SecurityRequirement(name = "JWT")
public class UserController {

    private final UserService userService;
    private final UserQueryService userQueryService;

    public UserController(UserService userService, UserQueryService userQueryService) {
        this.userService = userService;
        this.userQueryService = userQueryService;
    }

    @Operation(
        summary = "Listar usuários",
        description = "Retorna uma lista paginada de usuários do sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso",
            content = @Content(schema = @Schema(implementation = UserPageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<UserPageResponse> getUsers(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Tamanho da página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Campo para ordenação (id, username, role)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        UserPageResponse response = userQueryService.findAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
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