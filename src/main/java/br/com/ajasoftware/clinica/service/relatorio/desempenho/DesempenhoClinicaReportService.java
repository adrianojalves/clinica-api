package br.com.ajasoftware.clinica.service.relatorio.desempenho;

import br.com.ajasoftware.clinica.domain.dto.relatorio.desempenho.DesempenhoClinicaItemDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.desempenho.DesempenhoClinicaReportFilter;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.ProcedureType;
import br.com.ajasoftware.clinica.repository.AtendimentoConsultaExameRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.service.relatorio.ReportRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class DesempenhoClinicaReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AtendimentoRepository atendimentoRepository;
    private final AtendimentoConsultaExameRepository consultaExameRepository;
    private final ClinicRepository clinicRepository;
    private final ReportRenderingService reportRenderingService;

    @Transactional(readOnly = true)
    public byte[] generate(DesempenhoClinicaReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissaoInicial() != null
                ? filter.getDataEmissaoInicial().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissaoFinal() != null
                ? filter.getDataEmissaoFinal().atTime(LocalTime.MAX) : null;

        Long clinicaId = filter.getClinicaId();

        // Query 1: total de atendimentos por clínica (traz também o nome da clínica)
        Map<Long, String> nomeByClinica = new LinkedHashMap<>();
        Map<Long, Long> totalAtendimentosByClinica = new HashMap<>();
        for (Object[] row : atendimentoRepository.countAtendimentosByClinica(clinicaId, dataInicial, dataFinal)) {
            Long id = (Long) row[0];
            nomeByClinica.put(id, (String) row[1]);
            totalAtendimentosByClinica.put(id, (Long) row[2]);
        }

        // Query 2: faturamento bruto por clínica
        Map<Long, BigDecimal> faturamentoByClinica = new HashMap<>();
        for (Object[] row : atendimentoRepository.sumFaturamentoBrutoByClinica(clinicaId, dataInicial, dataFinal)) {
            faturamentoByClinica.put((Long) row[0], (BigDecimal) row[1]);
        }

        // Query 3: repasse por clínica
        Map<Long, BigDecimal> repasseByClinica = new HashMap<>();
        for (Object[] row : atendimentoRepository.sumRepasseByClinica(clinicaId, dataInicial, dataFinal)) {
            repasseByClinica.put((Long) row[0], (BigDecimal) row[1]);
        }

        // Query 4: consultas realizadas por clínica
        Map<Long, Long> consultasByClinica = new HashMap<>();
        for (Object[] row : consultaExameRepository.countItensByClinicaAndType(ProcedureType.CONSULTA, clinicaId, dataInicial, dataFinal)) {
            consultasByClinica.put((Long) row[0], (Long) row[1]);
        }

        // Query 5: exames e procedimentos por clínica (excluindo CONSULTA)
        Map<Long, Long> examesByClinica = new HashMap<>();
        for (Object[] row : consultaExameRepository.countItensByClinicaExcludingType(ProcedureType.CONSULTA, clinicaId, dataInicial, dataFinal)) {
            examesByClinica.put((Long) row[0], (Long) row[1]);
        }

        List<DesempenhoClinicaItemDTO> itens = buildItens(nomeByClinica, totalAtendimentosByClinica,
                faturamentoByClinica, repasseByClinica, consultasByClinica, examesByClinica);

        Map<String, Object> vars = new HashMap<>();
        vars.put("itens", itens);
        vars.put("filtrosAplicados", buildFilterSummary(filter));
        vars.put("grandTotais", buildGrandTotais(itens));

        return reportRenderingService.render("financeiro/desempenho/relatorio-desempenho-clinica", vars);
    }

    private List<DesempenhoClinicaItemDTO> buildItens(
            Map<Long, String> nomeByClinica,
            Map<Long, Long> totalAtendimentos,
            Map<Long, BigDecimal> faturamento,
            Map<Long, BigDecimal> repasse,
            Map<Long, Long> consultas,
            Map<Long, Long> exames) {

        List<DesempenhoClinicaItemDTO> itens = new ArrayList<>();

        for (Map.Entry<Long, String> entry : nomeByClinica.entrySet()) {
            Long id = entry.getKey();
            long atendimentos = totalAtendimentos.getOrDefault(id, 0L);
            BigDecimal fat = faturamento.getOrDefault(id, BigDecimal.ZERO);
            BigDecimal rep = repasse.getOrDefault(id, BigDecimal.ZERO);
            long cons = consultas.getOrDefault(id, 0L);
            long exam = exames.getOrDefault(id, 0L);

            BigDecimal ticket = atendimentos > 0
                    ? fat.divide(BigDecimal.valueOf(atendimentos), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            itens.add(new DesempenhoClinicaItemDTO(id, entry.getValue(), atendimentos, fat, rep, cons, exam, ticket));
        }

        return itens;
    }

    private Map<String, Object> buildGrandTotais(List<DesempenhoClinicaItemDTO> itens) {
        long sumAtendimentos = 0;
        BigDecimal sumFaturamento = BigDecimal.ZERO;
        BigDecimal sumRepasse = BigDecimal.ZERO;
        long sumConsultas = 0;
        long sumExames = 0;

        for (DesempenhoClinicaItemDTO item : itens) {
            sumAtendimentos += item.totalAtendimentos();
            sumFaturamento = sumFaturamento.add(item.faturamentoBruto());
            sumRepasse = sumRepasse.add(item.repasse());
            sumConsultas += item.consultasRealizadas();
            sumExames += item.examesProcedimentos();
        }

        BigDecimal ticketGeral = sumAtendimentos > 0
                ? sumFaturamento.divide(BigDecimal.valueOf(sumAtendimentos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> totais = new HashMap<>();
        totais.put("sumAtendimentos", sumAtendimentos);
        totais.put("sumFaturamento", sumFaturamento);
        totais.put("sumRepasse", sumRepasse);
        totais.put("sumConsultas", sumConsultas);
        totais.put("sumExames", sumExames);
        totais.put("ticketGeral", ticketGeral);
        return totais;
    }

    private Map<String, String> buildFilterSummary(DesempenhoClinicaReportFilter filter) {
        Map<String, String> summary = new LinkedHashMap<>();

        if (filter.getClinicaId() != null) {
            String name = clinicRepository.findById(filter.getClinicaId())
                    .map(c -> c.getName()).orElse("ID " + filter.getClinicaId());
            summary.put("Clínica", name);
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
