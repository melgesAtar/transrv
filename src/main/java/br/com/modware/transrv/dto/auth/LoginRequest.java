package br.com.modware.transrv.dto.auth;

public record LoginRequest(
    String username,
    String password
) {}

