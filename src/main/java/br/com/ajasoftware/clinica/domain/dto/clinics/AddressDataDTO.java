package br.com.ajasoftware.clinica.domain.dto.clinics;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for receiving address data from the frontend.
 */
public record AddressDataDTO(
        @NotBlank(message = "O logradouro é obrigatório.")
        String logradouro,

        @NotBlank(message = "O bairro é obrigatório.")
        String bairro,

        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(regexp = "\\d{8}", message = "O CEP deve conter exatamente 8 dígitos.")
        String cep,

        @NotBlank(message = "A cidade é obrigatória.")
        String cidade,

        @NotBlank(message = "A UF é obrigatória.")
        @Pattern(regexp = "[A-Z]{2}", message = "A UF deve conter exatamente 2 letras maiúsculas.")
        String uf,

        String complemento,

        @NotBlank(message = "O número é obrigatório.")
        String numero
) {}