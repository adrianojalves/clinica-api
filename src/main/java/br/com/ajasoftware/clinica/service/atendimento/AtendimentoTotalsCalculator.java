package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Single-Responsibility component responsible exclusively for recalculating
 * the financial totals of an Atendimento based on its current items.
 *
 * Keeping this logic isolated means both AtendimentoService (header CRUD) and
 * the future AtendimentoConsultaExameService item-CRUD operations can reuse it
 * without duplicating or coupling business logic.
 */
@Component
public class AtendimentoTotalsCalculator {

    /**
     * Sums all items and writes the four total fields back into the Atendimento entity.
     * Must be called inside an active transaction before the entity is flushed.
     *
     * @param atendimento the master entity whose totals will be updated in place.
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

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
