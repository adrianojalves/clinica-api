package br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AtendimentoSinteticoItensReportItemDTO(
        LocalDate dataAtendimento,
        String pacienteNome,
        String exames,
        BigDecimal valor,
        String formasPagamento,
        String clinicaNome
) {}
