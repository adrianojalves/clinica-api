package br.com.ajasoftware.clinica.domain.filter.clinics;

import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.*;

/**
 * class holding dynamic search parameters for Clinics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClinicFilter extends FilterBase {
    private String cnpj;
}