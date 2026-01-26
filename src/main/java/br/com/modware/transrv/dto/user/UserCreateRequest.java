package br.com.modware.transrv.dto.user;

import br.com.modware.transrv.model.Role;

public record UserCreateRequest(
    String username,
    String password,
    Role role
) {
    
}
