package br.com.ajasoftware.clinica.domain.dto.relatorio.historico;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HistoricoPacienteReportFilter {

    private Long clienteId;
    private Long clinicaId;
    private Long doctorId;
    private LocalDate dataEmissaoInicial;
    private LocalDate dataEmissaoFinal;
}
