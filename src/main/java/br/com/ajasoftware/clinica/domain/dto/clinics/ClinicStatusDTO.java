package br.com.ajasoftware.clinica.domain.dto.clinics;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for partial updates of the clinic's status.
 */
public record ClinicStatusDTO(
        @NotNull(message = "O status (ativo/inativo) é obrigatório.")
        Boolean active
) {}