package br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;

import java.math.BigDecimal;

public record AtendimentoPagamentoReportItemDTO(
        Long atendimentoId,
        String clienteName,
        String clinicaName,
        TipoPagamento tipoPagamento,
        Integer parcelas,
        BigDecimal valor,
        BigDecimal valorDesconto
) {
    public BigDecimal total() {
        BigDecimal v = valor != null ? valor : BigDecimal.ZERO;
        BigDecimal d = valorDesconto != null ? valorDesconto : BigDecimal.ZERO;
        return v.subtract(d);
    }

    public String tipoPagamentoLabel() {
        return switch (tipoPagamento) {
            case DINHEIRO -> "Dinheiro";
            case CARTAO_CREDITO -> "Cartão Crédito";
            case CARTAO_DEBITO -> "Cartão Débito";
            case PIX -> "PIX";
        };
    }
}
