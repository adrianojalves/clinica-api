package br.com.ajasoftware.clinica.service.medical.procedures;

import br.com.ajasoftware.clinica.domain.dto.medical.procedures.ProcedureRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.medical.procedures.ProcedureResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import br.com.ajasoftware.clinica.domain.filter.medical.procedure.ProcedureFilterDTO;
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
public class MedicalProcedureService {

    private final MedicalProcedureRepository repository;

    @Transactional(readOnly = true)
    public Page<ProcedureResponseDTO> listWithFilters(ProcedureFilterDTO filter, Pageable pageable) {
        return repository.findActiveWithFilters(filter, pageable)
                .map(ProcedureResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public ProcedureResponseDTO getById(Long id) {
        MedicalProcedure procedure = findEntityById(id);
        return new ProcedureResponseDTO(procedure);
    }

    @Transactional
    public ProcedureResponseDTO create(ProcedureRequestDTO data) {
        MedicalProcedure procedure = new MedicalProcedure();
        updateEntityData(procedure, data);

        repository.save(procedure);
        return new ProcedureResponseDTO(procedure);
    }

    @Transactional
    public ProcedureResponseDTO update(Long id, ProcedureRequestDTO data) {
        MedicalProcedure procedure = findEntityById(id);
        updateEntityData(procedure, data);

        return new ProcedureResponseDTO(procedure);
    }

    @Transactional
    public void delete(Long id) {
        MedicalProcedure procedure = findEntityById(id);
        procedure.setActive(false); // Soft delete
    }

    /**
     * Helper method to map DTO to Entity.
     */
    private void updateEntityData(MedicalProcedure entity, ProcedureRequestDTO dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setType(dto.type());
        entity.setTransferValue(dto.transferValue());
        entity.setPrice(dto.price());
    }

    /**
     * Helper method to find an entity or throw 404.
     */
    private MedicalProcedure findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedimento não encontrado."));
    }
}