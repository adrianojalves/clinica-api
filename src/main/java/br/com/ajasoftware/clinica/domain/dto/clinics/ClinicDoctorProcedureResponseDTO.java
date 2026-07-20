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
        String procedureTag,
        BigDecimal transferValue,
        BigDecimal price,
        BigDecimal transferValueCard,
        BigDecimal priceCard,
        BigDecimal pricePartner,
        String codigoClinica
) {
    public ClinicDoctorProcedureResponseDTO(Long id, Long clinicId, String clinicName, Long doctorId, String doctorName, Long medicalProcedureId, String procedureName, String procedureType, String procedureTag, BigDecimal transferValue, BigDecimal price, BigDecimal transferValueCard, BigDecimal priceCard, BigDecimal pricePartner) {
        this(id, clinicId, clinicName, doctorId, doctorName, medicalProcedureId, procedureName, procedureType, procedureTag, transferValue, price, transferValueCard, priceCard, pricePartner, null);
    }

    public ClinicDoctorProcedureResponseDTO(ClinicDoctorProcedure entity) {
        this(
                entity.getId(),
                entity.getClinic().getId(),
                entity.getClinic().getName(),
                entity.getDoctor()!=null?entity.getDoctor().getId():0L,
                entity.getDoctor()!=null?entity.getDoctor().getName():"",
                entity.getMedicalProcedure().getId(),
                entity.getMedicalProcedure().getName(),
                entity.getMedicalProcedure().getType().name(),
                entity.getMedicalProcedure().getTag(),
                entity.getTransferValue(),
                entity.getPrice(),
                entity.getTransferValueCard(),
                entity.getPriceCard(),
                entity.getPricePartner(),
                entity.getCodigoClinica()
        );
    }
}