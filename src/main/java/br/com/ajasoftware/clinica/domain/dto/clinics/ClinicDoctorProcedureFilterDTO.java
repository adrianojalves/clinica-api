package br.com.ajasoftware.clinica.domain.dto.clinics;

import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicDoctorProcedureFilterDTO extends FilterBase {

    // --- search by ID (kept for programmatic / internal use) ---
    private Long clinicId;
    private Long doctorId;
    private Long medicalProcedureId;

    // --- search by name (used by the front-end search UI) ---
    private String clinicName;
    private String doctorName;
    private String procedureName;
}