package br.com.ajasoftware.clinica.domain.dto.clinics;

import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.entity.clinics.PeriodPayment;

import java.math.BigDecimal;

/**
 * DTO for outgoing clinic data.
 * Maps the Entity to a clear JSON structure, including the embedded address.
 */
public record ClinicResponseDTO(
        Long id,
        String name,
        String cnpj,
        String fone1,
        String fone2,
        String site,
        String email,
        Boolean active,
        BigDecimal percentual,
        PeriodPayment periodPayment,
        AddressDataDTO address
) {
    public ClinicResponseDTO(Clinic clinic) {
        this(
                clinic.getId(),
                clinic.getName(),
                clinic.getCnpj(),
                clinic.getFone1(),
                clinic.getFone2(),
                clinic.getSite(),
                clinic.getEmail(),
                clinic.getActive(),
                clinic.getPercentual(),
                clinic.getPeriodPayment(),
                clinic.getAddress() != null ? new AddressDataDTO(
                        clinic.getAddress().getLogradouro(),
                        clinic.getAddress().getBairro(),
                        clinic.getAddress().getCep(),
                        clinic.getAddress().getCidade(),
                        clinic.getAddress().getUf(),
                        clinic.getAddress().getComplemento(),
                        clinic.getAddress().getNumero()
                ) : null
        );
    }
}