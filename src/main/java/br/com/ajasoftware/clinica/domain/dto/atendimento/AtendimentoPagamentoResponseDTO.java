package br.com.ajasoftware.clinica.domain.dto.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;

import java.math.BigDecimal;

public record AtendimentoPagamentoResponseDTO(
        Long id,
        Long codAtendimento,
        TipoPagamento tipoPagamento,
        BigDecimal valor,
        BigDecimal valorDesconto,
        Integer parcelas
) {
    public AtendimentoPagamentoResponseDTO(AtendimentoPagamento entity) {
        this(
                entity.getId(),
                entity.getAtendimento().getId(),
                entity.getTipoPagamento(),
                entity.getValor(),
                entity.getValorDesconto(),
                entity.getParcelas()
        );
    }
}
