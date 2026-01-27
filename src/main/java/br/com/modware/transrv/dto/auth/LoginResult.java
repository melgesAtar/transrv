package br.com.modware.transrv.dto.auth;

public record LoginResult(
    String token,
    AuthResponse authResponse
) {}
