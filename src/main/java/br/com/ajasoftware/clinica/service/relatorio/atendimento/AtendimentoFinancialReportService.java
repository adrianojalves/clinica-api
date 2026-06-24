package br.com.ajasoftware.clinica.service.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoPagamentoReportItemDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoReportFilter;
import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoReportItemDTO;
import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoReportType;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.repository.AtendimentoPagamentoRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.repository.ClientRepository;
import br.com.ajasoftware.clinica.repository.ClinicRepository;
import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.relatorio.ReportRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AtendimentoFinancialReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AtendimentoRepository atendimentoRepository;
    private final AtendimentoPagamentoRepository pagamentoRepository;
    private final ClinicRepository clinicRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ReportRenderingService reportRenderingService;

    @Transactional(readOnly = true)
    public byte[] generate(AtendimentoReportFilter filter) {
        return switch (filter.getTipo()) {
            case SINTETICO -> generateSintetico(filter);
            case ANALITICO_ITENS -> generateAnaliticoItens(filter);
            case ANALITICO_FORMA_PAGAMENTO -> generateAnaliticoFormaPagamento(filter);
        };
    }

    private byte[] generateSintetico(AtendimentoReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissaoInicial() != null
                ? filter.getDataEmissaoInicial().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissaoFinal() != null
                ? filter.getDataEmissaoFinal().atTime(LocalTime.MAX) : null;

        List<AtendimentoReportItemDTO> itens = atendimentoRepository.findForReport(
                filter.getStatus(),
                filter.getClinicaId(),
                filter.getClienteId(),
                filter.getUsuarioId(),
                dataInicial,
                dataFinal);

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalDesconto = BigDecimal.ZERO;
        BigDecimal totalAcrescimo = BigDecimal.ZERO;
        BigDecimal totalGeral = BigDecimal.ZERO;

        for (AtendimentoReportItemDTO item : itens) {
            totalPrice = totalPrice.add(item.totalPrice() != null ? item.totalPrice() : BigDecimal.ZERO);
            totalDesconto = totalDesconto.add(item.valorDesconto() != null ? item.valorDesconto() : BigDecimal.ZERO);
            totalAcrescimo = totalAcrescimo.add(item.valorAcrescimo() != null ? item.valorAcrescimo() : BigDecimal.ZERO);
            totalGeral = totalGeral.add(item.totalGeral());
        }

        Map<String, String> filtrosAplicados = buildFilterSummary(filter);

        Map<String, Object> vars = new HashMap<>();
        vars.put("itens", itens);
        vars.put("filtrosAplicados", filtrosAplicados);
        vars.put("sumTotalPrice", totalPrice);
        vars.put("sumValorDesconto", totalDesconto);
        vars.put("sumValorAcrescimo", totalAcrescimo);
        vars.put("sumTotalGeral", totalGeral);

        return reportRenderingService.render("financeiro/atendimento/relatorio-sintetico", vars);
    }

    private byte[] generateAnaliticoItens(AtendimentoReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissaoInicial() != null
                ? filter.getDataEmissaoInicial().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissaoFinal() != null
                ? filter.getDataEmissaoFinal().atTime(LocalTime.MAX) : null;

        List<Atendimento> atendimentos = atendimentoRepository.findEntitiesForReport(
                filter.getStatus(),
                filter.getClinicaId(),
                filter.getClienteId(),
                filter.getUsuarioId(),
                dataInicial,
                dataFinal);

        atendimentos.forEach(a -> a.getItens().forEach(item -> {
            if (item.getMedicalProcedure() != null) item.getMedicalProcedure().getName();
            if (item.getDoctor() != null) item.getDoctor().getName();
        }));

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalDesconto = BigDecimal.ZERO;
        BigDecimal totalAcrescimo = BigDecimal.ZERO;
        BigDecimal totalGeral = BigDecimal.ZERO;

        for (Atendimento a : atendimentos) {
            totalPrice = totalPrice.add(a.getTotalPrice() != null ? a.getTotalPrice() : BigDecimal.ZERO);
            totalDesconto = totalDesconto.add(a.getValorDesconto() != null ? a.getValorDesconto() : BigDecimal.ZERO);
            totalAcrescimo = totalAcrescimo.add(a.getValorAcrescimo() != null ? a.getValorAcrescimo() : BigDecimal.ZERO);
            BigDecimal geral = (a.getTotalPrice() != null ? a.getTotalPrice() : BigDecimal.ZERO)
                    .subtract(a.getValorDesconto() != null ? a.getValorDesconto() : BigDecimal.ZERO)
                    .add(a.getValorAcrescimo() != null ? a.getValorAcrescimo() : BigDecimal.ZERO);
            totalGeral = totalGeral.add(geral);
        }

        Map<String, String> filtrosAplicados = buildFilterSummary(filter);

        Map<String, Object> vars = new HashMap<>();
        vars.put("atendimentos", atendimentos);
        vars.put("filtrosAplicados", filtrosAplicados);
        vars.put("sumTotalPrice", totalPrice);
        vars.put("sumValorDesconto", totalDesconto);
        vars.put("sumValorAcrescimo", totalAcrescimo);
        vars.put("sumTotalGeral", totalGeral);

        return reportRenderingService.render("financeiro/atendimento/relatorio-analitico-itens", vars);
    }

    private byte[] generateAnaliticoFormaPagamento(AtendimentoReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissaoInicial() != null
                ? filter.getDataEmissaoInicial().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissaoFinal() != null
                ? filter.getDataEmissaoFinal().atTime(LocalTime.MAX) : null;

        List<AtendimentoPagamentoReportItemDTO> itens = pagamentoRepository.findPagamentosForReport(
                filter.getStatus(),
                filter.getClinicaId(),
                filter.getClienteId(),
                filter.getUsuarioId(),
                dataInicial,
                dataFinal);

        BigDecimal sumValor = BigDecimal.ZERO;
        BigDecimal sumDesconto = BigDecimal.ZERO;
        BigDecimal sumTotal = BigDecimal.ZERO;

        for (AtendimentoPagamentoReportItemDTO item : itens) {
            sumValor = sumValor.add(item.valor() != null ? item.valor() : BigDecimal.ZERO);
            sumDesconto = sumDesconto.add(item.valorDesconto() != null ? item.valorDesconto() : BigDecimal.ZERO);
            sumTotal = sumTotal.add(item.total());
        }

        Map<String, String> filtrosAplicados = buildFilterSummary(filter);

        Map<String, Object> vars = new HashMap<>();
        vars.put("itens", itens);
        vars.put("filtrosAplicados", filtrosAplicados);
        vars.put("sumValor", sumValor);
        vars.put("sumDesconto", sumDesconto);
        vars.put("sumTotal", sumTotal);

        return reportRenderingService.render("financeiro/atendimento/relatorio-analitico-pagamentos", vars);
    }

    private Map<String, String> buildFilterSummary(AtendimentoReportFilter filter) {
        Map<String, String> summary = new LinkedHashMap<>();

        String statusLabel = switch (filter.getStatus()) {
            case ABERTO -> "Aberto";
            case ENCAMINHADO -> "Encaminhado";
        };
        summary.put("Status", statusLabel);

        if (filter.getClinicaId() != null) {
            String name = clinicRepository.findById(filter.getClinicaId())
                    .map(c -> c.getName())
                    .orElse("ID " + filter.getClinicaId());
            summary.put("Clínica", name);
        }

        if (filter.getClienteId() != null) {
            String name = clientRepository.findById(filter.getClienteId())
                    .map(c -> c.getName())
                    .orElse("ID " + filter.getClienteId());
            summary.put("Cliente", name);
        }

        if (filter.getUsuarioId() != null) {
            String name = userRepository.findById(filter.getUsuarioId())
                    .map(u -> u.getName())
                    .orElse("ID " + filter.getUsuarioId());
            summary.put("Atendente", name);
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
