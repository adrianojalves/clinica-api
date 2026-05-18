package br.com.ajasoftware.clinica.domain.dto.users;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO responsible for receiving user update data.
 */
public record UserUpdateRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        String phone,

        boolean active,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String password,

        @NotNull(message = "O Percentual de desconto é obrigatório")
        BigDecimal  percentualDesconto,

        @NotEmpty(message = "O usuário deve ter pelo menos um perfil de acesso.")
        List<Long> roleIds
) {}