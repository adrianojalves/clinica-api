package br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class AtendimentoDiarioReportFilter {
    private Long clinicaId;
    private Long clienteId;
    private Long usuarioId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataEmissao;

    private AtendimentoStatus status;
}
