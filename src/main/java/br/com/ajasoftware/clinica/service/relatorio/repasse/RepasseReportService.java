package br.com.ajasoftware.clinica.service.relatorio.repasse;

import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseClinicaGroupDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportFilter;
import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportItemDTO;
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

        List<RepasseClinicaGroupDTO> grupos = buildGroups(itens);

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

    private List<RepasseClinicaGroupDTO> buildGroups(List<RepasseReportItemDTO> itens) {
        LinkedHashMap<String, List<RepasseReportItemDTO>> byClinica = new LinkedHashMap<>();
        for (RepasseReportItemDTO item : itens) {
            byClinica.computeIfAbsent(item.clinicaName(), k -> new ArrayList<>()).add(item);
        }

        List<RepasseClinicaGroupDTO> grupos = new ArrayList<>();
        for (Map.Entry<String, List<RepasseReportItemDTO>> entry : byClinica.entrySet()) {
            List<RepasseReportItemDTO> groupItens = entry.getValue();
            BigDecimal sumTotalPrice = BigDecimal.ZERO;
            BigDecimal sumValorAcrescimo = BigDecimal.ZERO;
            BigDecimal sumValorDesconto = BigDecimal.ZERO;
            BigDecimal sumTotalGeral = BigDecimal.ZERO;
            BigDecimal sumTotalTransferido = BigDecimal.ZERO;
            BigDecimal sumSaldo = BigDecimal.ZERO;

            for (RepasseReportItemDTO item : groupItens) {
                sumTotalPrice = sumTotalPrice.add(item.totalPrice() != null ? item.totalPrice() : BigDecimal.ZERO);
                sumValorAcrescimo = sumValorAcrescimo.add(item.valorAcrescimo() != null ? item.valorAcrescimo() : BigDecimal.ZERO);
                sumValorDesconto = sumValorDesconto.add(item.valorDesconto() != null ? item.valorDesconto() : BigDecimal.ZERO);
                sumTotalGeral = sumTotalGeral.add(item.totalGeral());
                sumTotalTransferido = sumTotalTransferido.add(item.totalTransferido());
                sumSaldo = sumSaldo.add(item.saldo());
            }

            grupos.add(new RepasseClinicaGroupDTO(
                    entry.getKey(),
                    groupItens,
                    sumTotalPrice,
                    sumValorAcrescimo,
                    sumValorDesconto,
                    sumTotalGeral,
                    sumTotalTransferido,
                    sumSaldo));
        }
        return grupos;
    }

    private Map<String, String> buildFilterSummary(RepasseReportFilter filter) {
        Map<String, String> summary = new LinkedHashMap<>();

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
