package br.com.ajasoftware.clinica.domain.dto.clinics;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import br.com.ajasoftware.clinica.domain.entity.clinics.PeriodPayment;

import java.math.BigDecimal;

/**
 * DTO for receiving clinic update requests.
 */
public record ClinicUpdateDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O telefone principal é obrigatório.")
        String fone1,

        String fone2,
        String site,

        @Email(message = "Formato de e-mail inválido.")
        String email,

        @DecimalMin(value = "0.0", inclusive = true, message = "O percentual não pode ser negativo.")
        @DecimalMax(value = "100.0", inclusive = true, message = "O percentual não pode ser maior que 100.")
        BigDecimal percentual,

        PeriodPayment periodPayment,
        Long codigoGuia,

        @NotNull(message = "Os dados de endereço são obrigatórios.")
        @Valid
        AddressDataDTO address
) {
    public ClinicUpdateDTO(String name, String fone1, String fone2, String site, String email, BigDecimal percentual, PeriodPayment periodPayment, AddressDataDTO address) {
        this(name, fone1, fone2, site, email, percentual, periodPayment, null, address);
    }
}