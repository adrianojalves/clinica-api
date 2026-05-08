package br.com.ajasoftware.clinica.domain.dto.security;

import java.util.List;

/**
 * DTO for sending the short-lived access token back to the client.
 */
public record AuthenticationResponseDTO(
        String accessToken,
        List<String> roles
) {}