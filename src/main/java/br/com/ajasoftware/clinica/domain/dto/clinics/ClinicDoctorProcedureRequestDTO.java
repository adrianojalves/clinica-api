package br.com.ajasoftware.clinica.domain.dto.clinics;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ClinicDoctorProcedureRequestDTO(
        @NotNull(message = "A clínica é obrigatória.")
        Long clinicId,

        Long doctorId,

        @NotNull(message = "O procedimento/exame é obrigatório.")
        Long medicalProcedureId,

        @DecimalMin(value = "0.0", message = "O valor de repasse não pode ser negativo.")
        BigDecimal transferValue,

        @NotNull(message = "O valor total é obrigatório.")
        @DecimalMin(value = "0.0", message = "O valor total não pode ser negativo.")
        BigDecimal price,

        @DecimalMin(value = "0.0", message = "O valor de repasse de cartão não pode ser negativo.")
        BigDecimal transferValueCard,

        @DecimalMin(value = "0.0", message = "O valor total de cartão não pode ser negativo.")
        BigDecimal priceCard,

        @DecimalMin(value = "0.0", message = "O valor parceiro não pode ser negativo.")
        BigDecimal pricePartner
) {
        public ClinicDoctorProcedureRequestDTO {
                if (doctorId != null && doctorId == 0) {
                        doctorId = null;
                }
        }
}