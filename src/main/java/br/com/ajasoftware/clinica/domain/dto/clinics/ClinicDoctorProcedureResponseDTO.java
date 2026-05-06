package br.com.ajasoftware.clinica.domain.dto.clinics;

import br.com.ajasoftware.clinica.domain.entity.clinics.ClinicDoctorProcedure;

import java.math.BigDecimal;

public record ClinicDoctorProcedureResponseDTO(
        Long id,
        Long clinicId,
        String clinicName,
        Long doctorId,
        String doctorName,
        Long medicalProcedureId,
        String procedureName,
        String procedureType,
        BigDecimal transferValue,
        BigDecimal price
) {
    public ClinicDoctorProcedureResponseDTO(ClinicDoctorProcedure entity) {
        this(
                entity.getId(),
                entity.getClinic().getId(),
                entity.getClinic().getName(),
                entity.getDoctor().getId(),
                entity.getDoctor().getName(),
                entity.getMedicalProcedure().getId(),
                entity.getMedicalProcedure().getName(),
                entity.getMedicalProcedure().getType().name(),
                entity.getTransferValue(),
                entity.getPrice()
        );
    }
}