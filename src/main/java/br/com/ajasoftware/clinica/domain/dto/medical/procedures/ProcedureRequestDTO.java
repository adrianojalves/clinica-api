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

        @DecimalMin(value = "0.0", message = "O valor de repasse não pode ser negativo.")
        BigDecimal transferValue,

        @NotNull(message = "O valor total do procedimento é obrigatório.")
        @DecimalMin(value = "0.0", message = "O valor total não pode ser negativo.")
        BigDecimal price
) {}