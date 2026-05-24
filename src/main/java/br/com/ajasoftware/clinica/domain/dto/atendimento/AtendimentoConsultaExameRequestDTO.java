package br.com.ajasoftware.clinica.domain.dto.atendimento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for an individual Atendimento item (consultation or exam).
 * Used both when creating/updating the Atendimento and when adding isolated items in the future.
 */
public record AtendimentoConsultaExameRequestDTO(

        Long codMedico,

        @NotNull(message = "O procedimento/exame é obrigatório.")
        Long codMedicalProcedure,

        @DecimalMin(value = "0.0", message = "O repasse (dinheiro) não pode ser negativo.")
        BigDecimal transferValue,

        @NotNull(message = "O valor (dinheiro) é obrigatório.")
        @DecimalMin(value = "0.0", message = "O valor (dinheiro) não pode ser negativo.")
        BigDecimal price,

        @DecimalMin(value = "0.0", message = "O repasse (cartão) não pode ser negativo.")
        BigDecimal transferValueCard,

        @NotNull(message = "O valor (cartão) é obrigatório.")
        @DecimalMin(value = "0.0", message = "O valor (cartão) não pode ser negativo.")
        BigDecimal priceCard
) {}
