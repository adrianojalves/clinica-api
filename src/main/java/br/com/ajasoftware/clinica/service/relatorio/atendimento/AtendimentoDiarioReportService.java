package br.com.ajasoftware.clinica.service.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoDiarioReportFilter;
import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoDiarioReportItemDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.atendimento.TipoPagamento;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtendimentoDiarioReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AtendimentoRepository atendimentoRepository;
    private final AtendimentoPagamentoRepository pagamentoRepository;
    private final ClinicRepository clinicRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ReportRenderingService reportRenderingService;

    @Transactional(readOnly = true)
    public byte[] generate(AtendimentoDiarioReportFilter filter) {
        LocalDateTime dataInicial = filter.getDataEmissao() != null
                ? filter.getDataEmissao().atStartOfDay() : null;
        LocalDateTime dataFinal = filter.getDataEmissao() != null
                ? filter.getDataEmissao().atTime(LocalTime.MAX) : null;

        List<Atendimento> atendimentos = atendimentoRepository.findEntitiesForReport(
                filter.getStatus(),
                filter.getClinicaId(),
                filter.getClienteId(),
                filter.getUsuarioId(),
                dataInicial,
                dataFinal);

        List<AtendimentoDiarioReportItemDTO> itens = atendimentos.stream().map(a -> {
            // Touch items inside transactional context to load lazily if needed
            a.getItens().forEach(item -> {
                if (item.getMedicalProcedure() != null) item.getMedicalProcedure().getName();
            });

            BigDecimal totalRepasse = a.getItens().stream()
                    .map(item -> item.getTransferValue() != null ? item.getTransferValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal valor;
            BigDecimal acrescimo;
            BigDecimal desconto;
            BigDecimal totalGeral;
            String formasPagamento;

            if (a.getStatus() == AtendimentoStatus.ENCAMINHADO) {
                List<AtendimentoPagamento> pagamentos = pagamentoRepository.findByAtendimentoId(a.getId());

                BigDecimal somaPagamentos = pagamentos.stream()
                        .map(p -> p.getValor() != null ? p.getValor() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                valor = a.getTotalPrice() != null ? a.getTotalPrice() : BigDecimal.ZERO;

                desconto = pagamentos.stream()
                        .map(p -> p.getValorDesconto() != null ? p.getValorDesconto() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                acrescimo = a.getValorAcrescimo() != null ? a.getValorAcrescimo() : BigDecimal.ZERO;

                totalGeral = valor.add(acrescimo).subtract(desconto);

                formasPagamento = pagamentos.stream()
                        .map(AtendimentoPagamento::getTipoPagamento)
                        .filter(Objects::nonNull)
                        .map(this::getTipoPagamentoLabel)
                        .distinct()
                        .collect(Collectors.joining(", "));

                if (formasPagamento.isBlank()) {
                    formasPagamento = "-";
                }
            } else {
                // ABERTO: use AtendimentoConsultaExame
                valor = a.getItens().stream()
                        .map(item -> item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                acrescimo = BigDecimal.ZERO;
                desconto = BigDecimal.ZERO;
                totalGeral = valor;
                formasPagamento = "-";
            }

            String servicos = a.getItens().stream()
                    .map(item -> item.getMedicalProcedure() != null ? item.getMedicalProcedure().getName() : "")
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.joining(", "));

            return new AtendimentoDiarioReportItemDTO(
                    a.getId(),
                    a.getCliente() != null ? a.getCliente().getName() : "",
                    a.getClinica() != null ? a.getClinica().getName() : "",
                    a.getDataEmissao(),
                    formasPagamento,
                    valor,
                    acrescimo,
                    desconto,
                    totalGeral,
                    totalRepasse,
                    servicos
            );
        }).toList();

        BigDecimal sumValor = itens.stream().map(AtendimentoDiarioReportItemDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumAcrescimo = itens.stream().map(AtendimentoDiarioReportItemDTO::acrescimo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumDesconto = itens.stream().map(AtendimentoDiarioReportItemDTO::desconto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumTotalGeral = itens.stream().map(AtendimentoDiarioReportItemDTO::totalGeral).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumTotalRepasse = itens.stream().map(AtendimentoDiarioReportItemDTO::totalRepasse).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, String> filtrosAplicados = buildFilterSummary(filter);

        Map<String, Object> vars = new HashMap<>();
        vars.put("itens", itens);
        vars.put("filtrosAplicados", filtrosAplicados);
        vars.put("sumValor", sumValor);
        vars.put("sumAcrescimo", sumAcrescimo);
        vars.put("sumDesconto", sumDesconto);
        vars.put("sumTotalGeral", sumTotalGeral);
        vars.put("sumTotalRepasse", sumTotalRepasse);

        return reportRenderingService.render("financeiro/atendimento/relatorio-atendimento-diario", vars);
    }

    private String getTipoPagamentoLabel(TipoPagamento tipo) {
        if (tipo == null) return "";
        return switch (tipo) {
            case DINHEIRO -> "Dinheiro";
            case CARTAO_CREDITO -> "Cartão Crédito";
            case CARTAO_DEBITO -> "Cartão Débito";
            case PIX -> "PIX";
        };
    }

    private Map<String, String> buildFilterSummary(AtendimentoDiarioReportFilter filter) {
        Map<String, String> summary = new LinkedHashMap<>();

        if (filter.getStatus() != null) {
            String statusLabel = switch (filter.getStatus()) {
                case ABERTO -> "Aberto";
                case ENCAMINHADO -> "Encaminhado";
            };
            summary.put("Status", statusLabel);
        } else {
            summary.put("Status", "Todos");
        }

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

        if (filter.getDataEmissao() != null) {
            summary.put("Data de Emissão", filter.getDataEmissao().format(DATE_FMT));
        }

        return summary;
    }
}
