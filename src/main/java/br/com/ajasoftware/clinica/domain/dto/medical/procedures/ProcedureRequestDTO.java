package br.com.ajasoftware.clinica.domain.dto.medical.procedures;

import br.com.ajasoftware.clinica.domain.entity.medical.procedures.ProcedureType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProcedureRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        String description,

        @NotNull(message = "O tipo (EXAME ou CONSULTA) é obrigatório.")
        ProcedureType type,

        Boolean active
) {}