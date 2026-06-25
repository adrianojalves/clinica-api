package br.com.ajasoftware.clinica.domain.dto.relatorio.desempenho;

import java.math.BigDecimal;

public record DesempenhoClinicaItemDTO(
        Long clinicaId,
        String clinicaName,
        long totalAtendimentos,
        BigDecimal faturamentoBruto,
        BigDecimal repasse,
        long consultasRealizadas,
        long examesProcedimentos,
        BigDecimal ticketMedio
) {}
