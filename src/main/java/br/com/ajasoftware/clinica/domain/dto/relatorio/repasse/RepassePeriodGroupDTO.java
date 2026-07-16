package br.com.ajasoftware.clinica.domain.dto.relatorio.repasse;

import java.math.BigDecimal;
import java.util.List;

public record RepassePeriodGroupDTO(
        String periodLabel,
        List<RepasseReportItemDTO> itens,
        BigDecimal sumTotalPrice,
        BigDecimal sumValorAcrescimo,
        BigDecimal sumValorDesconto,
        BigDecimal sumTotalGeral,
        BigDecimal sumTotalTransferido,
        BigDecimal sumSaldo
) {}
