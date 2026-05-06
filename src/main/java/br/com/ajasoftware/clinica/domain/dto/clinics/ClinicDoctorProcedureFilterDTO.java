package br.com.ajasoftware.clinica.domain.dto.clinics;

import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicDoctorProcedureFilterDTO extends FilterBase {
    private Long clinicId;
    private Long doctorId;
    private Long medicalProcedureId;
}