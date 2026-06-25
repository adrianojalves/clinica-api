package br.com.ajasoftware.clinica.domain.dto.relatorio.historico;

import java.util.List;

public record HistoricoPacienteClinicaGroupDTO(
        String clinicaName,
        List<HistoricoPacienteItemDTO> itens
) {}
