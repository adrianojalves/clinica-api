package br.com.ajasoftware.clinica.domain.dto.users;

import br.com.ajasoftware.clinica.domain.entity.Role;

public record RoleResponseDTO(
        Long id,
        String name
){
    public RoleResponseDTO(Role role) {
        this(
            role.getId(),
            role.getName()
        );
    }
}