package br.com.ajasoftware.clinica.domain.dto.security;

/**
 * DTO for sending the short-lived access token back to the client.
 */
public record AuthenticationResponseDTO(String accessToken) {}