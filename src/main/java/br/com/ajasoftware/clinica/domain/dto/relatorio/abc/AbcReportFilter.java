package br.com.ajasoftware.clinica.domain.dto.relatorio.abc;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AbcReportFilter {

    private Long clinicaId;
    private Long doctorId;
    private LocalDate dataEmissaoInicial;
    private LocalDate dataEmissaoFinal;
}
