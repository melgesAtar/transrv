package br.com.modware.transrv.service;

import br.com.modware.transrv.model.User;
import br.com.modware.transrv.repository.UserRepository;
import br.com.modware.transrv.dto.auth.AuthResponse;
import br.com.modware.transrv.dto.auth.ChangePasswordRequest;
import br.com.modware.transrv.dto.auth.LoginRequest;
import br.com.modware.transrv.dto.auth.LoginResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));
        
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }
        
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        AuthResponse authResponse = new AuthResponse(user.getUsername(), user.getRole().name());
        
        return new LoginResult(token, authResponse);
    }

    public AuthResponse getCurrentUser(String token) {
        if (token == null) {
            throw new RuntimeException("Token não fornecido");
        }
        
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);
        return new AuthResponse(username, role);
    }

    @Transactional
    public void changePassword(String token, ChangePasswordRequest request) {
        if (token == null) {
            throw new RuntimeException("Token não fornecido");
        }
        
        String username = jwtService.extractUsername(token);
        
        if (!jwtService.validateToken(token, username)) {
            throw new RuntimeException("Token inválido");
        }
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new RuntimeException("Senha antiga incorreta");
        }
        
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
