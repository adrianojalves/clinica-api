package br.com.ajasoftware.clinica.domain.dto.relatorio.repasse;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RepasseReportFilter {

    private Long clinicaId;
    private LocalDate dataEmissaoInicial;
    private LocalDate dataEmissaoFinal;
}
