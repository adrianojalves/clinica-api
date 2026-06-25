package br.com.ajasoftware.clinica.domain.dto.relatorio.desempenho;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DesempenhoClinicaReportFilter {

    private Long clinicaId;
    private LocalDate dataEmissaoInicial;
    private LocalDate dataEmissaoFinal;
}
