package br.com.ajasoftware.clinica.domain.filter.medical.procedure;

import br.com.ajasoftware.clinica.domain.entity.medical.procedures.ProcedureType;
import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.Getter;
import lombok.Setter;

/**
 * Filter object to bind query parameters from the GET request.
 * Extends FilterBase to inherit common filter properties.
 */
@Getter
@Setter
public class ProcedureFilterDTO extends FilterBase {
    private ProcedureType type;

}