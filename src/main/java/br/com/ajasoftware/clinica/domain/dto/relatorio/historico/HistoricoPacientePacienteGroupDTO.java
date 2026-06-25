package br.com.ajasoftware.clinica.domain.dto.relatorio.historico;

import java.util.List;

public record HistoricoPacientePacienteGroupDTO(
        String clienteName,
        List<HistoricoPacienteClinicaGroupDTO> clinicas
) {}
