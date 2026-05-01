package br.com.ajasoftware.clinica.domain.dto.clinics;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CNPJ;

/**
 * DTO for receiving new clinic creation requests.
 */
public record ClinicRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O CNPJ é obrigatório.")
        @CNPJ(message = "O CNPJ informado é inválido.")
        String cnpj,

        @NotBlank(message = "O telefone principal é obrigatório.")
        String fone1,

        String fone2,
        String site,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        @NotNull(message = "Os dados de endereço são obrigatórios.")
        @Valid
        AddressDataDTO address
) {}