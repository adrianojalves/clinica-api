package br.com.ajasoftware.clinica.domain.dto.client;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import br.com.ajasoftware.clinica.domain.entity.client.BiologicalSex;
import br.com.ajasoftware.clinica.domain.entity.client.SexualOrientation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating an existing client.
 * All fields are provided to allow full or partial updates via PUT.
 */
public record ClientUpdateDTO(
        @NotBlank(message = "O nome do cliente é obrigatório.")
        String name,

        String socialName,

        @NotBlank(message = "O RG é obrigatório.")
        String rg,

        String cpf, // Optional in update, but service handles hash if provided

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