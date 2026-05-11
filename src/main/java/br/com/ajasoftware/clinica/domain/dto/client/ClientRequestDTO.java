package br.com.ajasoftware.clinica.domain.dto.client;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import br.com.ajasoftware.clinica.domain.entity.client.BiologicalSex;
import br.com.ajasoftware.clinica.domain.entity.client.SexualOrientation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for incoming client creation requests.
 * Fields like biological sex and name are mandatory.
 */
public record ClientRequestDTO(
        @NotBlank(message = "O nome do cliente é obrigatório.")
        String name,

        String socialName,

        @NotBlank(message = "O RG é obrigatório.")
        String rg,

        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        String phone,

        @Email(message = "O e-mail informado é inválido.")
        String email,

        @NotNull(message = "O sexo biológico é obrigatório.")
        BiologicalSex biologicalSex,

        SexualOrientation sexualOrientation,

        @NotNull(message = "Os dados de endereço são obrigatórios.")
        @Valid
        AddressDataDTO address
) {}