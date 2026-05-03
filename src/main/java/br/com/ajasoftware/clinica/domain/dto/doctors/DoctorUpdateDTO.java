package br.com.ajasoftware.clinica.domain.dto.doctors;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating an existing doctor.
 */
public record DoctorUpdateDTO(
        @NotBlank(message = "O nome do médico é obrigatório.")
        String name,

        String crm,

        @NotNull(message = "Os dados de endereço são obrigatórios.")
        @Valid
        AddressDataDTO address
) {}