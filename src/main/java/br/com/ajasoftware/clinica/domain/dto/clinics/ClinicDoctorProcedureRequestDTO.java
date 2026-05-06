package br.com.ajasoftware.clinica.domain.dto.clinics;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ClinicDoctorProcedureRequestDTO(
        @NotNull(message = "A clínica é obrigatória.")
        Long clinicId,

        @NotNull(message = "O médico é obrigatório.")
        Long doctorId,

        @NotNull(message = "O procedimento/exame é obrigatório.")
        Long medicalProcedureId,

        @DecimalMin(value = "0.0", message = "O valor de repasse não pode ser negativo.")
        BigDecimal transferValue,

        @NotNull(message = "O valor total é obrigatório.")
        @DecimalMin(value = "0.0", message = "O valor total não pode ser negativo.")
        BigDecimal price
) {}