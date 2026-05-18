package br.com.ajasoftware.clinica.domain.dto.users;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO responsible for receiving user creation data from the client.
 */
public record UserRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O login é obrigatório.")
        String login,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String password,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        String phone,

        @NotNull(message = "O Percentual de desconto é obrigatório")
        BigDecimal percentualDesconto,

        @NotEmpty(message = "O usuário deve ter pelo menos um perfil de acesso.")
        List<Long> roleIds
) {}