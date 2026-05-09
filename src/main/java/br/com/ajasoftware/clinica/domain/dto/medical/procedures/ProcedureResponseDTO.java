package br.com.ajasoftware.clinica.domain.dto.medical.procedures;

import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.ProcedureType;

import java.math.BigDecimal;

public record ProcedureResponseDTO(
        Long id,
        String name,
        String description,
        ProcedureType type,
        Boolean active
) {
    public ProcedureResponseDTO(MedicalProcedure procedure) {
        this(
                procedure.getId(),
                procedure.getName(),
                procedure.getDescription(),
                procedure.getType(),
                procedure.getActive()
        );
    }
}