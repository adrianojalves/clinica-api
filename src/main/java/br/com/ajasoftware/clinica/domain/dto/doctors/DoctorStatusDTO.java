package br.com.ajasoftware.clinica.domain.dto.doctors;

import jakarta.validation.constraints.NotNull;

public record DoctorStatusDTO(
        @NotNull(message = "O status (ativo/inativo) é obrigatório.")
        Boolean active
) {}