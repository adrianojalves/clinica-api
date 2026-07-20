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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import br.com.ajasoftware.clinica.utils.ExcelUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClinicDoctorProcedureService {

    private final ClinicDoctorProcedureRepository repository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalProcedureRepository medicalProcedureRepository;

    @Transactional(readOnly = true)
    public Page<ClinicDoctorProcedureResponseDTO> listWithFilters(ClinicDoctorProcedureFilterDTO filter, Pageable pageable) {
        if (filter.getClinicId() != null && filter.getClinicId() <= 0) {
            filter.setClinicId(null);
        }
        if (filter.getDoctorId() != null && filter.getDoctorId() <= 0) {
            filter.setDoctorId(null);
        }
        if (filter.getMedicalProcedureId() != null && filter.getMedicalProcedureId() <= 0) {
            filter.setMedicalProcedureId(null);
        }
        if (filter.getClinicName() != null && filter.getClinicName().isBlank()) {
            filter.setClinicName(null);
        }
        if (filter.getDoctorName() != null && filter.getDoctorName().isBlank()) {
            filter.setDoctorName(null);
        }
        if (filter.getProcedureName() != null && filter.getProcedureName().isBlank()) {
            filter.setProcedureName(null);
        }
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
        entity.setPricePartner(data.pricePartner() != null ? data.pricePartner() : data.price());
        entity.setCodigoClinica(data.codigoClinica());
    }

    private ClinicDoctorProcedure findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuração de procedimento não encontrada."));
    }

    @Transactional(readOnly = true)
    public byte[] exportToExcel(Long clinicId) {
        List<ClinicDoctorProcedure> procedures = repository.findByClinicId(clinicId);

        String[] headers = {
                "ID", "ID Clínica", "Nome Clínica", "ID Dr", "Nome DR",
                "ID Procedimento", "Nome Procedimento", "Repasse", "Valor", "Valor Clínica", "Código Clínica"
        };

        return ExcelUtils.exportToExcel("Procedures", headers, procedures, (proc, row) -> {
            // id
            row.createCell(0).setCellValue(proc.getId());
            // clinic.id
            row.createCell(1).setCellValue(proc.getClinic().getId());
            // clinic.name
            row.createCell(2).setCellValue(proc.getClinic().getName());

            // doctor.id (0 se não existir)
            long docId = proc.getDoctor() != null ? proc.getDoctor().getId() : 0L;
            row.createCell(3).setCellValue(docId);

            // doctor.name (vazio se não existir)
            String docName = proc.getDoctor() != null ? proc.getDoctor().getName() : "";
            row.createCell(4).setCellValue(docName);

            // medicalProcedure.id
            row.createCell(5).setCellValue(proc.getMedicalProcedure().getId());
            // medicalProcedure.name
            row.createCell(6).setCellValue(proc.getMedicalProcedure().getName());

            // transferValue (valor repasse)
            double transfer = proc.getTransferValue() != null ? proc.getTransferValue().doubleValue() : 0.0;
            row.createCell(7).setCellValue(transfer);

            // price
            double priceVal = proc.getPrice() != null ? proc.getPrice().doubleValue() : 0.0;
            row.createCell(8).setCellValue(priceVal);

            // pricePartner (0 se não existir conteúdo)
            double partner = proc.getPricePartner() != null ? proc.getPricePartner().doubleValue() : 0.0;
            row.createCell(9).setCellValue(partner);

            // codigoClinica
            row.createCell(10).setCellValue(proc.getCodigoClinica() != null ? proc.getCodigoClinica() : "");
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
                    continue; // Skip if no ID
                }

                ClinicDoctorProcedure entity = repository.findById(id).orElse(null);
                if (entity == null) {
                    continue; // Skip if entity not found
                }

                BigDecimal transferValueExcel = ExcelUtils.getBigDecimalCellValue(row.getCell(7));
                BigDecimal priceExcel = ExcelUtils.getBigDecimalCellValue(row.getCell(8));
                BigDecimal pricePartnerExcel = ExcelUtils.getBigDecimalCellValue(row.getCell(9));
                String codigoClinicaExcel = ExcelUtils.getStringCellValue(row.getCell(10));

                boolean modified = false;

                if (areDifferent(entity.getTransferValue(), transferValueExcel)) {
                    entity.setTransferValue(transferValueExcel);
                    modified = true;
                }

                if (areDifferent(entity.getPrice(), priceExcel)) {
                    entity.setPrice(priceExcel);
                    modified = true;
                }

                if (areDifferent(entity.getPricePartner(), pricePartnerExcel)) {
                    entity.setPricePartner(pricePartnerExcel);
                    modified = true;
                }

                if (row.getLastCellNum() >= 11 && !java.util.Objects.equals(entity.getCodigoClinica(), codigoClinicaExcel)) {
                    entity.setCodigoClinica(codigoClinicaExcel);
                    modified = true;
                }

                if (modified) {
                    repository.save(entity);
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao processar o arquivo Excel: " + e.getMessage(), e);
        }
    }

    private boolean areDifferent(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null && bd2 == null) return false;
        if (bd1 == null || bd2 == null) return true;
        return bd1.compareTo(bd2) != 0;
    }
}