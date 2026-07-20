package br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AtendimentoDiarioReportItemDTO(
        Long id,
        String clienteNome,
        String clinicaNome,
        LocalDateTime dataEmissao,
        String formasPagamento,
        BigDecimal valor,
        BigDecimal acrescimo,
        BigDecimal desconto,
        BigDecimal totalGeral,
        BigDecimal totalRepasse
) {}
