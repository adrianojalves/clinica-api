package br.com.ajasoftware.clinica.domain.dto.users;

public record UserSummaryDTO(
        Long id,
        String name,
        String phone,
        boolean active
) {}
