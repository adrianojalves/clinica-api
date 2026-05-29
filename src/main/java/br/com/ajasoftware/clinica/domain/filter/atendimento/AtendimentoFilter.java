package br.com.ajasoftware.clinica.domain.filter.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.filter.FilterBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoFilter extends FilterBase {

    private Long clienteId;
    private String nomeCliente;
    private Long clinicaId;
    private String nomeClinica;
    private Long usuarioId;
    private String nomeUsuario;
    private AtendimentoStatus status;
    private LocalDate dataConsultaExameInicio;
    private LocalDate dataConsultaExameFim;
}
