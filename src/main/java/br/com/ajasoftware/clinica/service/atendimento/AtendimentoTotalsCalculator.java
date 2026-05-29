package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Single-Responsibility component responsible exclusively for recalculating
 * the financial totals of an Atendimento based on its current items and payments.
 */
@Component
public class AtendimentoTotalsCalculator {

    /**
     * Sums all itens and writes the four total fields back into the Atendimento entity.
     * Must be called inside an active transaction before the entity is flushed.
     */
    public void recalculate(Atendimento atendimento) {
        List<AtendimentoConsultaExame> itens = atendimento.getItens();

        BigDecimal totalTransferValue = itens.stream()
                .map(i -> nullSafe(i.getTransferValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrice = itens.stream()
                .map(i -> nullSafe(i.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTransferValueCard = itens.stream()
                .map(i -> nullSafe(i.getTransferValueCard()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPriceCard = itens.stream()
                .map(i -> nullSafe(i.getPriceCard()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        atendimento.setTotalTransferValue(totalTransferValue);
        atendimento.setTotalPrice(totalPrice);
        atendimento.setTotalTransferValueCard(totalTransferValueCard);
        atendimento.setTotalPriceCard(totalPriceCard);
    }

    /**
     * Recalculates itens totals and then applies payment-based financial fields:
     * valorDesconto (sum of discounts) and valorAcrescimo (amount paid above the total).
     * Must be called inside an active transaction before the entity is flushed.
     */
    public void recalculateWithPayments(Atendimento atendimento, List<AtendimentoPagamento> pagamentos) {
        recalculate(atendimento);

        BigDecimal totalDescontos = pagamentos.stream()
                .map(p -> nullSafe(p.getValorDesconto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagamentosLiquido = pagamentos.stream()
                .map(p -> nullSafe(p.getValor()).subtract(nullSafe(p.getValorDesconto())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        atendimento.setValorDesconto(totalDescontos);

        BigDecimal acrescimo = totalPagamentosLiquido.subtract(atendimento.getTotalPrice());
        atendimento.setValorAcrescimo(acrescimo.compareTo(BigDecimal.ZERO) > 0 ? acrescimo : BigDecimal.ZERO);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
