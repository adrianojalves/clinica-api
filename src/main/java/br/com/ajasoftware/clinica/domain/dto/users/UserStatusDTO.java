package br.com.ajasoftware.clinica.domain.dto.users;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for parcial update of status (active/deactive).
 */
public record UserStatusDTO(
        @NotNull(message = "O status (ativo/inativo) é obrigatório.")
        Boolean active
) {}