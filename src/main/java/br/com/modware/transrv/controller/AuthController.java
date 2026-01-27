package br.com.modware.transrv.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;
import br.com.modware.transrv.service.AuthService;
import br.com.modware.transrv.dto.auth.AuthResponse;
import br.com.modware.transrv.dto.auth.ChangePasswordRequest;
import br.com.modware.transrv.dto.auth.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de usuários")
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Realizar login",
        description = "Autentica um usuário e retorna um token JWT armazenado em cookie HttpOnly"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciais de login")
            @RequestBody LoginRequest request,
            HttpServletResponse response) {
        try {
            var loginResult = authService.login(request);
            
            Cookie cookie = new Cookie("jwt", loginResult.token());
            cookie.setHttpOnly(true);
            cookie.setSecure(true); 
            cookie.setPath("/");
            cookie.setMaxAge(86400); 
            cookie.setAttribute("SameSite", "None");
            response.addCookie(cookie);
            
            return ResponseEntity.ok(loginResult.authResponse());
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @Operation(
        summary = "Realizar logout",
        description = "Remove o token JWT do cookie, efetivando o logout do usuário"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Obter usuário atual",
        description = "Retorna informações do usuário autenticado baseado no token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido ou ausente")
    })
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            @Parameter(description = "Token JWT do cookie", required = false)
            @CookieValue(value = "jwt", required = false) String token) {
        try {
            AuthResponse response = authService.getCurrentUser(token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @Operation(
        summary = "Alterar senha",
        description = "Altera a senha do usuário autenticado. Requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Senha antiga incorreta"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou ausente")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Senha antiga e nova senha")
            @RequestBody ChangePasswordRequest request,
            @Parameter(description = "Token JWT do cookie", required = false)
            @CookieValue(value = "jwt", required = false) String token) {
        try {
            authService.changePassword(token, request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Senha antiga incorreta")) {
                return ResponseEntity.status(400).build();
            }
            return ResponseEntity.status(401).build();
        }
    }
    
}
