package br.com.ajasoftware.clinica.domain.dto.relatorio.abc;

import java.math.BigDecimal;

public record AbcProcedimentoItemDTO(
        int rank,
        String procedimentoName,
        long quantidade,
        BigDecimal percentualTotal,
        BigDecimal percentualAcumulado
) {}
