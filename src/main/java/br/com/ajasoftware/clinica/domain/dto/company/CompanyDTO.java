package br.com.ajasoftware.clinica.domain.dto.company;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import br.com.ajasoftware.clinica.domain.entity.company.Company;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CNPJ;

public record CompanyDTO(
        Long id,

        @NotBlank(message = "A razão social é obrigatória.")
        String corporateName,

        @NotBlank(message = "O nome fantasia é obrigatório.")
        String tradeName,

        @NotBlank(message = "O CNPJ é obrigatório.")
        @CNPJ(message = "O CNPJ informado é inválido.")
        String cnpj,

        @NotBlank(message = "O telefone é obrigatório.")
        String phone,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        String logoUrl,

        @NotNull(message = "Os dados de endereço são obrigatórios.")
        @Valid
        AddressDataDTO address
) {
    public CompanyDTO(Company company) {
        this(
                company.getId(),
                company.getCorporateName(),
                company.getTradeName(),
                company.getCnpj(),
                company.getPhone(),
                company.getEmail(),
                company.getLogoUrl(),
                new AddressDataDTO(
                        company.getAddress().getLogradouro(),
                        company.getAddress().getBairro(),
                        company.getAddress().getCep(),
                        company.getAddress().getCidade(),
                        company.getAddress().getUf(),
                        company.getAddress().getComplemento(),
                        company.getAddress().getNumero()
                )
        );
    }
}