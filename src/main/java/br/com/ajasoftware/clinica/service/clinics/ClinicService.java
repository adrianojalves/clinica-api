package br.com.ajasoftware.clinica.service.clinics;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicUpdateDTO;
import br.com.ajasoftware.clinica.domain.entity.address.Address;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.filter.clinics.ClinicFilter;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class handling clinic-related business logic.
 */
@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    /**
     * Retrieves a paginated list of active clinics based on dynamic filters.
     */
    @Transactional(readOnly = true)
    public Page<ClinicResponseDTO> listAll(ClinicFilter filter, Pageable pageable) {
        return clinicRepository.findWithFilter(filter, pageable).map(ClinicResponseDTO::new);
    }

    /**
     * Verifies if a given CNPJ is already registered.
     */
    @Transactional(readOnly = true)
    public boolean checkCnpjExists(String cnpj) {
        return clinicRepository.existsByCnpj(cnpj);
    }

    /**
     * Updates the clinic's active status (Activate/Deactivate).
     */
    @Transactional
    public void updateStatus(Long id, Boolean active) {
        var clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operação falhou: Clínica não encontrada no sistema."));

        clinic.setActive(active);
    }

    /**
     * Creates a new clinic.
     * Validates CNPJ uniqueness before saving.
     */
    @Transactional
    public ClinicResponseDTO create(ClinicRequestDTO data) {
        if (clinicRepository.existsByCnpj(data.cnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Operação falhou: O CNPJ informado já está cadastrado.");
        }

        Clinic clinic = new Clinic();
        clinic.setName(data.name());
        clinic.setCnpj(data.cnpj());
        clinic.setFone1(data.fone1());
        clinic.setFone2(data.fone2());
        clinic.setSite(data.site());
        clinic.setEmail(data.email());
        clinic.setPercentual(data.percentual());

        Address address = new Address(
                data.address().logradouro(),
                data.address().bairro(),
                data.address().cep(),
                data.address().numero(),
                data.address().complemento(),
                data.address().cidade(),
                data.address().uf()
        );
        clinic.setAddress(address);

        clinicRepository.save(clinic);
        return new ClinicResponseDTO(clinic);
    }

    /**
     * Updates an existing clinic.
     * CNPJ is not modified during this operation.
     */
    @Transactional
    public ClinicResponseDTO update(Long id, ClinicUpdateDTO data) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operação falhou: Clínica não encontrada no sistema."));

        clinic.setName(data.name());
        clinic.setFone1(data.fone1());
        clinic.setFone2(data.fone2());
        clinic.setSite(data.site());
        clinic.setEmail(data.email());
        clinic.setPercentual(data.percentual());

        clinic.getAddress().updateInfo(data.address());

        return new ClinicResponseDTO(clinic);
    }
}