package br.com.ajasoftware.clinica.domain.dto.security;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for receiving login credentials from the client.
 */
public record AuthenticationRequestDTO(
        @NotBlank(message = "O login não pode estar vazio.")
        String login,

        @NotBlank(message = "A senha não pode estar vazia.")
        String password
) {}