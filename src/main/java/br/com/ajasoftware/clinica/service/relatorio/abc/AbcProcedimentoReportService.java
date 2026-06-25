package br.com.ajasoftware.clinica.service.relatorio.abc;

import br.com.ajasoftware.clinica.domain.dto.relatorio.abc.AbcCurvaGroupDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.abc.AbcProcedimentoItemDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.abc.AbcReportFilter;
import br.com.ajasoftware.clinica.repository.AtendimentoConsultaExameRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.repository.DoctorRepository;
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
public class AbcProcedimentoReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal LIMIT_A = new BigDecimal("70");
    private static final BigDecimal LIMIT_B = new BigDecimal("90");

    private final AtendimentoConsultaExameRepository consultaExameRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final ReportRenderingService reportRenderingService;

    @Transactional(readOnly = true)
    public byte[] generate(AbcReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissaoInicial() != null
                ? filter.getDataEmissaoInicial().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissaoFinal() != null
                ? filter.getDataEmissaoFinal().atTime(LocalTime.MAX) : null;

        List<Object[]> rows = consultaExameRepository.countByProcedimento(
                filter.getClinicaId(),
                filter.getDoctorId(),
                dataInicial,
                dataFinal);

        long totalGeral = rows.stream().mapToLong(r -> (Long) r[1]).sum();

        List<AbcCurvaGroupDTO> curvas = buildCurvas(rows, totalGeral);

        Map<String, Object> vars = new HashMap<>();
        vars.put("curvas", curvas);
        vars.put("totalGeral", totalGeral);
        vars.put("filtrosAplicados", buildFilterSummary(filter));

        return reportRenderingService.render("financeiro/abc/relatorio-abc-procedimentos", vars);
    }

    private List<AbcCurvaGroupDTO> buildCurvas(List<Object[]> rows, long totalGeral) {
        Map<String, List<AbcProcedimentoItemDTO>> grupos = new LinkedHashMap<>();
        grupos.put("A", new ArrayList<>());
        grupos.put("B", new ArrayList<>());
        grupos.put("C", new ArrayList<>());

        Map<String, Long> sumByGroup = new HashMap<>();
        sumByGroup.put("A", 0L);
        sumByGroup.put("B", 0L);
        sumByGroup.put("C", 0L);

        BigDecimal acumulado = BigDecimal.ZERO;
        int rank = 1;

        for (Object[] row : rows) {
            String nome = (String) row[0];
            long qtd = (Long) row[1];

            BigDecimal pct = totalGeral > 0
                    ? BigDecimal.valueOf(qtd * 100.0).divide(BigDecimal.valueOf(totalGeral), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            acumulado = acumulado.add(pct);

            String curva = acumulado.compareTo(LIMIT_A) <= 0 ? "A"
                    : acumulado.compareTo(LIMIT_B) <= 0 ? "B"
                    : "C";

            grupos.get(curva).add(new AbcProcedimentoItemDTO(rank++, nome, qtd, pct, acumulado));
            sumByGroup.merge(curva, qtd, Long::sum);
        }

        List<AbcCurvaGroupDTO> result = new ArrayList<>();
        for (String key : List.of("A", "B", "C")) {
            result.add(new AbcCurvaGroupDTO(key, grupos.get(key), sumByGroup.get(key)));
        }
        return result;
    }

    private Map<String, String> buildFilterSummary(AbcReportFilter filter) {
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
