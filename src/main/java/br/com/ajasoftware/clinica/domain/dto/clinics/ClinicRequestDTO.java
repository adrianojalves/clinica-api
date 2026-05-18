package br.com.ajasoftware.clinica.domain.dto.clinics;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CNPJ;

import java.math.BigDecimal;

/**
 * DTO for receiving new clinic creation requests.
 */
public record ClinicRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        String cnpj,

        @NotBlank(message = "O telefone principal é obrigatório.")
        String fone1,

        String fone2,
        String site,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        @DecimalMin(value = "0.0", inclusive = true, message = "O percentual não pode ser negativo.")
        @DecimalMax(value = "100.0", inclusive = true, message = "O percentual não pode ser maior que 100.")
        BigDecimal percentual,

        @NotNull(message = "Os dados de endereço são obrigatórios.")
        @Valid
        AddressDataDTO address
) {}