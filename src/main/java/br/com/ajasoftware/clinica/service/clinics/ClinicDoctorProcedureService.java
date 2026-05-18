package br.com.ajasoftware.clinica.service.clinics;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureFilterDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.entity.clinics.ClinicDoctorProcedure;
import br.com.ajasoftware.clinica.domain.entity.doctors.Doctor;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import br.com.ajasoftware.clinica.repository.ClinicDoctorProcedureRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.repository.DoctorRepository;
import br.com.ajasoftware.clinica.repository.MedicalProcedureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClinicDoctorProcedureService {

    private final ClinicDoctorProcedureRepository repository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalProcedureRepository medicalProcedureRepository;

    @Transactional(readOnly = true)
    public Page<ClinicDoctorProcedureResponseDTO>   listWithFilters(ClinicDoctorProcedureFilterDTO filter, Pageable pageable) {
        return repository.findWithFilters(filter, pageable)
                .map(ClinicDoctorProcedureResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public ClinicDoctorProcedureResponseDTO getById(Long id) {
        ClinicDoctorProcedure entity = findEntityById(id);
        return new ClinicDoctorProcedureResponseDTO(entity);
    }

    @Transactional
    public ClinicDoctorProcedureResponseDTO create(ClinicDoctorProcedureRequestDTO data) {
        validateUniqueTrio(data.clinicId(), data.doctorId(), data.medicalProcedureId(), null);

        ClinicDoctorProcedure entity = new ClinicDoctorProcedure();
        mapDtoToEntity(entity, data);

        repository.save(entity);
        return new ClinicDoctorProcedureResponseDTO(entity);
    }

    @Transactional
    public ClinicDoctorProcedureResponseDTO update(Long id, ClinicDoctorProcedureRequestDTO data) {
        ClinicDoctorProcedure entity = findEntityById(id);
        validateUniqueTrio(data.clinicId(), data.doctorId(), data.medicalProcedureId(), id);

        mapDtoToEntity(entity, data);

        return new ClinicDoctorProcedureResponseDTO(entity);
    }

    @Transactional
    public void delete(Long id) {
        ClinicDoctorProcedure entity = findEntityById(id);
        repository.delete(entity); // Hard delete for this relationship table
    }

    /**
     * Ensures that the combination of Clinic, Doctor, and Procedure is unique.
     */
    private void validateUniqueTrio(Long clinicId, Long doctorId, Long procedureId, Long idToIgnore) {
        boolean exists;
        if (idToIgnore == null) {
            exists = repository.existsCombination(clinicId, doctorId, procedureId);
        } else {
            exists = repository.existsCombinationForAnotherId(clinicId, doctorId, procedureId, idToIgnore);
        }

        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe uma configuração de valor para este Médico e Procedimento nesta Clínica.");
        }
    }

    /**
     * Maps DTO values to the Entity, fetching references from the database.
     */
    private void mapDtoToEntity(ClinicDoctorProcedure entity, ClinicDoctorProcedureRequestDTO data) {
        Clinic clinic = clinicRepository.findById(data.clinicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clínica não encontrada."));

        Doctor doctor = switch (data.doctorId()) {
            case null -> null;
            case Long id -> doctorRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado."));
        };

        MedicalProcedure procedure = medicalProcedureRepository.findById(data.medicalProcedureId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedimento não encontrado."));

        entity.setClinic(clinic);
        entity.setDoctor(doctor);
        entity.setMedicalProcedure(procedure);
        entity.setTransferValue(data.transferValue());
        entity.setPrice(data.price());
        entity.setTransferValueCard(data.transferValueCard());
        entity.setPriceCard(data.priceCard());
    }

    private ClinicDoctorProcedure findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuração de procedimento não encontrada."));
    }
}