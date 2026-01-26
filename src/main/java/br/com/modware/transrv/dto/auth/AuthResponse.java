package br.com.modware.transrv.dto.auth;

public record AuthResponse(
    String username,
    String role
) {}