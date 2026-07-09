package br.com.ajasoftware.clinica.domain.dto.log;

import jakarta.validation.constraints.NotBlank;

public record LogUpdateDTO(
        @NotBlank(message = "O conteúdo do log não pode ser vazio.")
        String log
) {}
