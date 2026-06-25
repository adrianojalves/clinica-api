package br.com.ajasoftware.clinica.domain.dto.relatorio.repasse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RepasseReportItemDTO(
        Long id,
        String clienteName,
        String clinicaName,
        LocalDateTime dataEmissao,
        BigDecimal totalPrice,
        BigDecimal valorAcrescimo,
        BigDecimal valorDesconto,
        BigDecimal totalTransferValue,
        BigDecimal totalTransferValueCard
) {
    private static BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    public BigDecimal totalGeral() {
        return safe(totalPrice).subtract(safe(valorDesconto)).add(safe(valorAcrescimo));
    }

    public BigDecimal totalTransferido() {
        return safe(totalTransferValue).add(safe(totalTransferValueCard));
    }

    public BigDecimal saldo() {
        return totalGeral().subtract(totalTransferido());
    }
}
