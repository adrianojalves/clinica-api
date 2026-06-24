package br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AtendimentoReportItemDTO(
        Long id,
        String clienteName,
        String clinicaName,
        String usuarioName,
        LocalDateTime dataEmissao,
        LocalDate dataConsultaExame,
        BigDecimal totalPrice,
        BigDecimal valorDesconto,
        BigDecimal valorAcrescimo
) {
    public BigDecimal totalGeral() {
        BigDecimal price = totalPrice != null ? totalPrice : BigDecimal.ZERO;
        BigDecimal desconto = valorDesconto != null ? valorDesconto : BigDecimal.ZERO;
        BigDecimal acrescimo = valorAcrescimo != null ? valorAcrescimo : BigDecimal.ZERO;
        return price.subtract(desconto).add(acrescimo);
    }
}
