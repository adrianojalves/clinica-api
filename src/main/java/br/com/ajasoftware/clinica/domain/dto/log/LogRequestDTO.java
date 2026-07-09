package br.com.ajasoftware.clinica.domain.dto.log;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogRequestDTO(
        @NotBlank(message = "O conteúdo do log não pode ser vazio.")
        String log,

        @NotNull(message = "O usuário deve ser informado.")
        Long codUsuario
) {}
