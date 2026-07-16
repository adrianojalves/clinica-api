package br.com.ajasoftware.clinica.service.relatorio.repasse;

import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseClinicaGroupDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepassePeriodGroupDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportFilter;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportItemDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportType;
import br.com.ajasoftware.clinica.domain.entity.clinics.PeriodPayment;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.service.relatorio.ReportRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
public class RepasseReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AtendimentoRepository atendimentoRepository;
    private final ClinicRepository clinicRepository;
    private final ReportRenderingService reportRenderingService;

    @Transactional(readOnly = true)
    public byte[] generate(RepasseReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissaoInicial() != null
                ? filter.getDataEmissaoInicial().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissaoFinal() != null
                ? filter.getDataEmissaoFinal().atTime(LocalTime.MAX) : null;

        List<RepasseReportItemDTO> itens = atendimentoRepository.findForRepasseReport(
                filter.getClinicaId(), dataInicial, dataFinal);

        RepasseReportType reportType = filter.getTipoRelatorio() != null ? filter.getTipoRelatorio() : RepasseReportType.SINTETICO;
        List<RepasseClinicaGroupDTO> grupos = buildGroups(itens, reportType);

        BigDecimal grandTotalPrice = BigDecimal.ZERO;
        BigDecimal grandValorAcrescimo = BigDecimal.ZERO;
        BigDecimal grandValorDesconto = BigDecimal.ZERO;
        BigDecimal grandTotalGeral = BigDecimal.ZERO;
        BigDecimal grandTotalTransferido = BigDecimal.ZERO;
        BigDecimal grandSaldo = BigDecimal.ZERO;

        for (RepasseClinicaGroupDTO g : grupos) {
            grandTotalPrice = grandTotalPrice.add(g.sumTotalPrice());
            grandValorAcrescimo = grandValorAcrescimo.add(g.sumValorAcrescimo());
            grandValorDesconto = grandValorDesconto.add(g.sumValorDesconto());
            grandTotalGeral = grandTotalGeral.add(g.sumTotalGeral());
            grandTotalTransferido = grandTotalTransferido.add(g.sumTotalTransferido());
            grandSaldo = grandSaldo.add(g.sumSaldo());
        }

        Map<String, String> filtrosAplicados = buildFilterSummary(filter);

        Map<String, Object> vars = new HashMap<>();
        vars.put("grupos", grupos);
        vars.put("filtrosAplicados", filtrosAplicados);
        vars.put("grandTotalPrice", grandTotalPrice);
        vars.put("grandValorAcrescimo", grandValorAcrescimo);
        vars.put("grandValorDesconto", grandValorDesconto);
        vars.put("grandTotalGeral", grandTotalGeral);
        vars.put("grandTotalTransferido", grandTotalTransferido);
        vars.put("grandSaldo", grandSaldo);

        return reportRenderingService.render("financeiro/repasse/relatorio-repasse", vars);
    }

    private List<RepasseClinicaGroupDTO> buildGroups(List<RepasseReportItemDTO> itens, RepasseReportType reportType) {
        LinkedHashMap<String, List<RepasseReportItemDTO>> byClinica = new LinkedHashMap<>();
        for (RepasseReportItemDTO item : itens) {
            byClinica.computeIfAbsent(item.clinicaName(), k -> new ArrayList<>()).add(item);
        }

        List<RepasseClinicaGroupDTO> grupos = new ArrayList<>();
        for (Map.Entry<String, List<RepasseReportItemDTO>> entry : byClinica.entrySet()) {
            List<RepasseReportItemDTO> groupItens = entry.getValue();
            List<RepassePeriodGroupDTO> periodGroups = partitionByPeriod(groupItens, reportType);

            BigDecimal sumTotalPrice = BigDecimal.ZERO;
            BigDecimal sumValorAcrescimo = BigDecimal.ZERO;
            BigDecimal sumValorDesconto = BigDecimal.ZERO;
            BigDecimal sumTotalGeral = BigDecimal.ZERO;
            BigDecimal sumTotalTransferido = BigDecimal.ZERO;
            BigDecimal sumSaldo = BigDecimal.ZERO;

            for (RepassePeriodGroupDTO pg : periodGroups) {
                sumTotalPrice = sumTotalPrice.add(pg.sumTotalPrice());
                sumValorAcrescimo = sumValorAcrescimo.add(pg.sumValorAcrescimo());
                sumValorDesconto = sumValorDesconto.add(pg.sumValorDesconto());
                sumTotalGeral = sumTotalGeral.add(pg.sumTotalGeral());
                sumTotalTransferido = sumTotalTransferido.add(pg.sumTotalTransferido());
                sumSaldo = sumSaldo.add(pg.sumSaldo());
            }

            grupos.add(new RepasseClinicaGroupDTO(
                    entry.getKey(),
                    periodGroups,
                    sumTotalPrice,
                    sumValorAcrescimo,
                    sumValorDesconto,
                    sumTotalGeral,
                    sumTotalTransferido,
                    sumSaldo));
        }
        return grupos;
    }

    private List<RepassePeriodGroupDTO> partitionByPeriod(List<RepasseReportItemDTO> itens, RepasseReportType reportType) {
        if (reportType != RepasseReportType.ANALITICO || itens.isEmpty()) {
            return List.of(createPeriodGroup(null, itens));
        }

        PeriodPayment period = itens.get(0).periodPayment();
        if (period == null || period == PeriodPayment.MENSAL) {
            return List.of(createPeriodGroup(null, itens));
        }

        List<RepasseReportItemDTO> sortedItens = new ArrayList<>(itens);
        sortedItens.sort((a, b) -> a.dataEmissao().compareTo(b.dataEmissao()));

        LinkedHashMap<String, List<RepasseReportItemDTO>> grouped = new LinkedHashMap<>();

        for (RepasseReportItemDTO item : sortedItens) {
            String label = getPeriodLabel(item, period);
            grouped.computeIfAbsent(label, k -> new ArrayList<>()).add(item);
        }

        List<RepassePeriodGroupDTO> periodGroups = new ArrayList<>();
        for (Map.Entry<String, List<RepasseReportItemDTO>> entry : grouped.entrySet()) {
            periodGroups.add(createPeriodGroup(entry.getKey(), entry.getValue()));
        }

        return periodGroups;
    }

    private String getPeriodLabel(RepasseReportItemDTO item, PeriodPayment period) {
        java.time.LocalDate date = item.dataEmissao().toLocalDate();
        switch (period) {
            case DIARIO:
                return "Dia " + date.format(DATE_FMT);
            case SEMANAL:
                java.time.LocalDate monday = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                java.time.LocalDate sunday = date.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
                return "Semana de " + monday.format(DATE_FMT) + " a " + sunday.format(DATE_FMT);
            case QUINZENAL:
                java.time.LocalDate start;
                java.time.LocalDate end;
                if (date.getDayOfMonth() <= 15) {
                    start = date.withDayOfMonth(1);
                    end = date.withDayOfMonth(15);
                } else {
                    start = date.withDayOfMonth(16);
                    end = date.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                }
                return "Quinzena de " + start.format(DATE_FMT) + " a " + end.format(DATE_FMT);
            default:
                return null;
        }
    }

    private RepassePeriodGroupDTO createPeriodGroup(String label, List<RepasseReportItemDTO> itens) {
        BigDecimal sumTotalPrice = BigDecimal.ZERO;
        BigDecimal sumValorAcrescimo = BigDecimal.ZERO;
        BigDecimal sumValorDesconto = BigDecimal.ZERO;
        BigDecimal sumTotalGeral = BigDecimal.ZERO;
        BigDecimal sumTotalTransferido = BigDecimal.ZERO;
        BigDecimal sumSaldo = BigDecimal.ZERO;

        for (RepasseReportItemDTO item : itens) {
            sumTotalPrice = sumTotalPrice.add(item.totalPrice() != null ? item.totalPrice() : BigDecimal.ZERO);
            sumValorAcrescimo = sumValorAcrescimo.add(item.valorAcrescimo() != null ? item.valorAcrescimo() : BigDecimal.ZERO);
            sumValorDesconto = sumValorDesconto.add(item.valorDesconto() != null ? item.valorDesconto() : BigDecimal.ZERO);
            sumTotalGeral = sumTotalGeral.add(item.totalGeral());
            sumTotalTransferido = sumTotalTransferido.add(item.totalTransferido());
            sumSaldo = sumSaldo.add(item.saldo());
        }

        return new RepassePeriodGroupDTO(
                label,
                itens,
                sumTotalPrice,
                sumValorAcrescimo,
                sumValorDesconto,
                sumTotalGeral,
                sumTotalTransferido,
                sumSaldo
        );
    }

    private Map<String, String> buildFilterSummary(RepasseReportFilter filter) {
        Map<String, String> summary = new LinkedHashMap<>();

        RepasseReportType type = filter.getTipoRelatorio() != null ? filter.getTipoRelatorio() : RepasseReportType.SINTETICO;
        summary.put("Tipo", type == RepasseReportType.ANALITICO ? "Analítico" : "Sintético");

        if (filter.getClinicaId() != null) {
            String name = clinicRepository.findById(filter.getClinicaId())
                    .map(c -> c.getName())
                    .orElse("ID " + filter.getClinicaId());
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
