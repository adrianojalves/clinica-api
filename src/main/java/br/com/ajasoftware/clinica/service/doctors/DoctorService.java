package br.com.ajasoftware.clinica.service.doctors;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorUpdateDTO;
import br.com.ajasoftware.clinica.domain.entity.address.Address;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;
import br.com.ajasoftware.clinica.domain.filter.doctors.DoctorFilter;
import br.com.ajasoftware.clinica.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> listWithFilter(DoctorFilter filter, Pageable pageable) {
        return doctorRepository.findWithFilter(filter, pageable)
                .map(DoctorResponseDTO::new);
    }

    @Transactional
    public DoctorResponseDTO create(DoctorRequestDTO data) {
        if (data.crm()!=null && doctorRepository.existsByCrm(data.crm())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Operação falhou: O CRM informado já está cadastrado.");
        }
        Doctor doctor = new Doctor();
        doctor.setName(data.name());
        doctor.setCrm(data.crm());
        doctor.setAddress(new Address(
                data.address().logradouro(),
                data.address().bairro(),
                data.address().cep(),
                data.address().numero(),
                data.address().complemento(),
                data.address().cidade(),
                data.address().uf()
        ));

        doctorRepository.save(doctor);
        return new DoctorResponseDTO(doctor);
    }

    @Transactional
    public DoctorResponseDTO update(Long id, DoctorUpdateDTO data) {
        Doctor doctor = getDoctorOrThrow(id);

        doctor.setName(data.name());
        doctor.getAddress().updateInfo(data.address());
        doctor.setCrm(data.crm());

        return new DoctorResponseDTO(doctor);
    }

    /**
     * Soft delete/Activation mechanism.
     * Updates only the status flag instead of hard-deleting the record.
     */
    @Transactional
    public void changeStatus(Long id, boolean newStatus) {
        Doctor doctor = getDoctorOrThrow(id);
        doctor.setActive(newStatus);
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getById(Long id) {
        Doctor doctor = getDoctorOrThrow(id);
        return new DoctorResponseDTO(doctor);
    }

    private Doctor getDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado no sistema."));
    }
}