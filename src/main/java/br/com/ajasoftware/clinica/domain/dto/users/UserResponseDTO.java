package br.com.ajasoftware.clinica.domain.dto.users;

import br.com.ajasoftware.clinica.domain.entity.Role;
import br.com.ajasoftware.clinica.domain.entity.User;

import java.util.List;

/**
 * DTO responsible for returning user data to the client safely.
 */
public record UserResponseDTO(
        Long id,
        String name,
        String login,
        String email,
        String phone,
        boolean active,
        List<String> roles
) {
    /**
     * Custom constructor to easily convert a User entity into this DTO.
     */
    public UserResponseDTO(User user) {
        this(
                user.getId(),
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                user.getRoles().stream().map(Role::getName).toList()
        );
    }
}