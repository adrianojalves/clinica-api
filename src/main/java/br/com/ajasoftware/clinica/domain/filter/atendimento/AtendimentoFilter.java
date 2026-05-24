package br.com.ajasoftware.clinica.domain.filter.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;
import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Filter class holding dynamic search parameters for Atendimento queries.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoFilter extends FilterBase {

    private Long clienteId;
    private Long clinicaId;
    private Long usuarioId;
    private AtendimentoStatus status;
    private TipoPagamento tipoPagamento;
    private LocalDate dataConsultaExameInicio;
    private LocalDate dataConsultaExameFim;
}
