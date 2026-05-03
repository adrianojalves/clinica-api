package br.com.ajasoftware.clinica.domain.dto.doctors;

import br.com.ajasoftware.clinica.domain.dto.clinics.AddressDataDTO;
import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;

/**
 * DTO for returning doctor data to the client.
 */
public record DoctorResponseDTO(
        Long id,
        String name,
        String crm,
        Boolean status,
        AddressDataDTO address
) {
    public DoctorResponseDTO(Doctor doctor) {
        this(
                doctor.getId(),
                doctor.getName(),
                doctor.getCrm(),
                doctor.getActive(),
                new AddressDataDTO(
                        doctor.getAddress().getLogradouro(),
                        doctor.getAddress().getBairro(),
                        doctor.getAddress().getCep(),
                        doctor.getAddress().getCidade(),
                        doctor.getAddress().getUf(),
                        doctor.getAddress().getComplemento(),
                        doctor.getAddress().getNumero()
                )
        );
    }
}