package br.com.ajasoftware.clinica.domain.filter.doctors;

import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.Getter;
import lombok.Setter;

/**
 * Filter specific for Doctor searches.
 * Inherits id and name from FilterBase.
 */
@Getter
@Setter
public class DoctorFilter extends FilterBase {
    private Boolean status;
}