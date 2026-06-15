package br.com.ajasoftware.clinica.domain.dto.atendimento;

import java.math.BigDecimal;

public record AtendimentoReciboItemDTO(
        String procedureName,
        BigDecimal originalPrice,
        BigDecimal discountAmount,
        BigDecimal adjustedPrice
) {}
