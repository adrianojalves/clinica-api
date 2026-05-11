package br.com.ajasoftware.clinica.domain.dto.client;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import br.com.ajasoftware.clinica.domain.entity.client.BiologicalSex;
import br.com.ajasoftware.clinica.domain.entity.client.Client;
import br.com.ajasoftware.clinica.domain.entity.client.SexualOrientation;

/**
 * DTO for returning client data.
 * Sensitive data is decrypted by the JPA Converter before reaching this record.
 */
public record ClientResponseDTO(
        Long id,
        String name,
        String socialName,
        String rg,
        String cpf,
        String phone,
        String email,
        BiologicalSex biologicalSex,
        SexualOrientation sexualOrientation,
        Boolean status,
        AddressDataDTO address
) {
    public ClientResponseDTO(Client client) {
        this(
                client.getId(),
                client.getName(),
                client.getSocialName(),
                client.getRg(),
                client.getCpf(),
                client.getPhone(),
                client.getEmail(),
                client.getBiologicalSex(),
                client.getSexualOrientation(),
                client.getActive(),
                client.getAddress() != null ? new AddressDataDTO(
                        client.getAddress().getLogradouro(),
                        client.getAddress().getBairro(),
                        client.getAddress().getCep(),
                        client.getAddress().getCidade(),
                        client.getAddress().getUf(),
                        client.getAddress().getComplemento(),
                        client.getAddress().getNumero()
                ) : null
        );
    }
}