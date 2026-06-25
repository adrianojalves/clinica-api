package br.com.ajasoftware.clinica.service.relatorio.historico;

import br.com.ajasoftware.clinica.domain.dto.relatorio.historico.HistoricoPacienteClinicaGroupDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.historico.HistoricoPacienteItemDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.historico.HistoricoPacientePacienteGroupDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.historico.HistoricoPacienteReportFilter;
import br.com.ajasoftware.clinica.repository.AtendimentoConsultaExameRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.repository.DoctorRepository;
import br.com.ajasoftware.clinica.service.relatorio.ReportRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoricoPacienteReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AtendimentoConsultaExameRepository consultaExameRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final ReportRenderingService reportRenderingService;

    @Transactional(readOnly = true)
    public byte[] generate(HistoricoPacienteReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissaoInicial() != null
                ? filter.getDataEmissaoInicial().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissaoFinal() != null
                ? filter.getDataEmissaoFinal().atTime(LocalTime.MAX) : null;

        List<HistoricoPacienteItemDTO> itens = consultaExameRepository.findHistoricoPaciente(
                filter.getClienteId(),
                filter.getClinicaId(),
                filter.getDoctorId(),
                dataInicial,
                dataFinal);

        List<HistoricoPacientePacienteGroupDTO> pacienteGrupos = buildGroups(itens);

        Map<String, Object> vars = new HashMap<>();
        vars.put("pacienteGrupos", pacienteGrupos);
        vars.put("filtrosAplicados", buildFilterSummary(filter));
        vars.put("totalItens", itens.size());

        return reportRenderingService.render("financeiro/historico/relatorio-historico-paciente", vars);
    }

    private List<HistoricoPacientePacienteGroupDTO> buildGroups(List<HistoricoPacienteItemDTO> itens) {
        LinkedHashMap<String, LinkedHashMap<String, List<HistoricoPacienteItemDTO>>> byPaciente = new LinkedHashMap<>();

        for (HistoricoPacienteItemDTO item : itens) {
            byPaciente
                    .computeIfAbsent(item.clienteName(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(item.clinicaName(), k -> new ArrayList<>())
                    .add(item);
        }

        List<HistoricoPacientePacienteGroupDTO> pacienteGrupos = new ArrayList<>();
        for (Map.Entry<String, LinkedHashMap<String, List<HistoricoPacienteItemDTO>>> pacienteEntry : byPaciente.entrySet()) {
            List<HistoricoPacienteClinicaGroupDTO> clinicaGrupos = new ArrayList<>();
            for (Map.Entry<String, List<HistoricoPacienteItemDTO>> clinicaEntry : pacienteEntry.getValue().entrySet()) {
                clinicaGrupos.add(new HistoricoPacienteClinicaGroupDTO(clinicaEntry.getKey(), clinicaEntry.getValue()));
            }
            pacienteGrupos.add(new HistoricoPacientePacienteGroupDTO(pacienteEntry.getKey(), clinicaGrupos));
        }
        return pacienteGrupos;
    }

    private Map<String, String> buildFilterSummary(HistoricoPacienteReportFilter filter) {
        Map<String, String> summary = new LinkedHashMap<>();

        if (filter.getClinicaId() != null) {
            String name = clinicRepository.findById(filter.getClinicaId())
                    .map(c -> c.getName()).orElse("ID " + filter.getClinicaId());
            summary.put("Clínica", name);
        }

        if (filter.getDoctorId() != null) {
            String name = doctorRepository.findById(filter.getDoctorId())
                    .map(d -> d.getName()).orElse("ID " + filter.getDoctorId());
            summary.put("Médico", name);
        }

        if (filter.getDataEmissaoInicial() != null && filter.getDataEmissaoFinal() != null) {
            summary.put("Período",
                    filter.getDataEmissaoInicial().format(DATE_FMT) + " a " + filter.getDataEmissaoFinal().format(DATE_FMT));
        } else if (filter.getDataEmissaoInicial() != null) {
            summary.put("A partir de", filter.getDataEmissaoInicial().format(DATE_FMT));
        } else if (filter.getDataEmissaoFinal() != null) {
            summary.put("Até", filter.getDataEmissaoFinal().format(DATE_FMT));
        }

        return summary;
    }
}
