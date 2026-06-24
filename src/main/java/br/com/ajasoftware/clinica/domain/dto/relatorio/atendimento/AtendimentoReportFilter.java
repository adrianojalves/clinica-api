package br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AtendimentoReportFilter {

    private Long clinicaId;
    private Long clienteId;
    private Long usuarioId;
    private LocalDate dataEmissaoInicial;
    private LocalDate dataEmissaoFinal;

    @NotNull(message = "O status é obrigatório.")
    private AtendimentoStatus status;

    @NotNull(message = "O tipo de relatório é obrigatório.")
    private AtendimentoReportType tipo;
}
