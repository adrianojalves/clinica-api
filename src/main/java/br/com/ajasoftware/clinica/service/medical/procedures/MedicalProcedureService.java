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
import br.com.ajasoftware.clinica.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MedicalProcedureService {

    private final MedicalProcedureRepository repository;

    @Transactional(readOnly = true)
    public Page<ProcedureResponseDTO> listWithFilters(ProcedureFilterDTO filter, Pageable pageable) {
        return repository.findWithFilters(filter, pageable)
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
        procedure.setActive(true);

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

    @Transactional(readOnly = true)
    public byte[] exportToExcel() {
        List<MedicalProcedure> procedures = repository.findAll();

        String[] headers = {
                "ID", "Nome", "Descrição", "Tipo", "Ativo", "Tag"
        };

        return ExcelUtils.exportToExcel("Procedimentos", headers, procedures, (proc, row) -> {
            row.createCell(0).setCellValue(proc.getId());
            row.createCell(1).setCellValue(proc.getName() != null ? proc.getName() : "");
            row.createCell(2).setCellValue(proc.getDescription() != null ? proc.getDescription() : "");
            row.createCell(3).setCellValue(proc.getType() != null ? proc.getType().name() : "");
            row.createCell(4).setCellValue(Boolean.TRUE.equals(proc.getActive()) ? "SIM" : "NÃO");
            row.createCell(5).setCellValue(proc.getTag() != null ? proc.getTag() : "");
        });
    }

    @Transactional
    public void importAndUpdateFromExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Planilha vazia ou inválida.");
            }

            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Long id = ExcelUtils.getLongCellValue(row.getCell(0));
                if (id == null) {
                    continue;
                }

                MedicalProcedure entity = repository.findById(id).orElse(null);
                if (entity == null) {
                    continue;
                }

                short lastCellNum = row.getLastCellNum();
                if (lastCellNum <= 0) {
                    continue;
                }

                int tagColIndex = (lastCellNum >= 6) ? 5 : (lastCellNum - 1);
                Cell tagCell = row.getCell(tagColIndex);
                String tagValue = ExcelUtils.getStringCellValue(tagCell);

                if (!Objects.equals(entity.getTag(), tagValue)) {
                    entity.setTag(tagValue);
                    repository.save(entity);
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao processar o arquivo Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to map DTO to Entity.
     */
    private void updateEntityData(MedicalProcedure entity, ProcedureRequestDTO dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setType(dto.type());
        if (dto.active() != null)
            entity.setActive(dto.active());
        entity.setTag(dto.tag());
    }

    /**
     * Helper method to find an entity or throw 404.
     */
    private MedicalProcedure findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedimento não encontrado."));
    }
}