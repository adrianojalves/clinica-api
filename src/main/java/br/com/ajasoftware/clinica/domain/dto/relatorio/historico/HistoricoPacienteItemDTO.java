package br.com.ajasoftware.clinica.domain.dto.relatorio.historico;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HistoricoPacienteItemDTO(
        Long atendimentoId,
        LocalDateTime dataEmissao,
        LocalDate dataConsulta,
        String procedimentoName,
        String doctorName,
        String clinicaName,
        String clienteName
) {}
