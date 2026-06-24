package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoReciboItemDTO;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.company.Company;
import br.com.ajasoftware.clinica.domain.entity.medical.procedures.MedicalProcedure;
import br.com.ajasoftware.clinica.repository.AtendimentoPagamentoRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.service.relatorio.ReportRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AtendimentoReportService {

    private final AtendimentoRepository atendimentoRepository;
    private final AtendimentoPagamentoRepository pagamentoRepository;
    private final ReportRenderingService reportRenderingService;

    @Transactional(readOnly = true)
    public byte[] generateEncaminhamento(Long id) {
        Atendimento atendimento = findOrThrow(id);
        Company company = reportRenderingService.getCompany();

        atendimento.getItens().forEach(item -> {
            if (item.getMedicalProcedure() != null) item.getMedicalProcedure().getName();
            if (item.getDoctor() != null) item.getDoctor().getName();
        });
        if (atendimento.getCliente() != null) atendimento.getCliente().getName();
        if (atendimento.getClinica() != null) atendimento.getClinica().getName();

        Map<String, Object> vars = new HashMap<>();
        vars.put("atendimento", atendimento);
        vars.put("isOrcamento", atendimento.getStatus() == AtendimentoStatus.ABERTO);
        vars.put("observacaoCompleta", buildCombinedObservation(company, atendimento));

        return reportRenderingService.render("atendimento/encaminhamento", vars);
    }

    @Transactional(readOnly = true)
    public byte[] generateRecibo(Long id) {
        Atendimento atendimento = findOrThrow(id);
        List<AtendimentoPagamento> pagamentos = pagamentoRepository.findByAtendimentoId(id);
        boolean hasCardPayment = pagamentos.stream()
                .anyMatch(p -> p.getTipoPagamento().isCartao());

        BigDecimal totalBruto = pagamentos.stream()
                .map(p -> p.getValor() != null ? p.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDesconto = pagamentos.stream()
                .map(p -> p.getValorDesconto() != null ? p.getValorDesconto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEfetivo = totalBruto.subtract(totalDesconto);

        List<AtendimentoConsultaExame> itens = atendimento.getItens();
        List<BigDecimal> originalPrices = itens.stream()
                .map(item -> {
                    BigDecimal p = resolveItemPrice(item, hasCardPayment);
                    return p != null ? p : BigDecimal.ZERO;
                })
                .toList();

        BigDecimal originalTotal = originalPrices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<BigDecimal> adjustedPrices = calculateProportional(originalPrices, originalTotal, totalEfetivo);

        List<AtendimentoReciboItemDTO> items = new ArrayList<>();
        for (int i = 0; i < itens.size(); i++) {
            MedicalProcedure mp = itens.get(i).getMedicalProcedure();
            BigDecimal original = originalPrices.get(i);
            BigDecimal adjusted = adjustedPrices.get(i);
            items.add(new AtendimentoReciboItemDTO(
                    mp != null ? mp.getName() : "—",
                    original,
                    original.subtract(adjusted),
                    adjusted));
        }

        atendimento.getItens().forEach(item -> {
            if (item.getMedicalProcedure() != null) item.getMedicalProcedure().getName();
        });
        if (atendimento.getCliente() != null) {
            atendimento.getCliente().getName();
            atendimento.getCliente().getCpf();
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("atendimento", atendimento);
        vars.put("pagamentos", pagamentos);
        vars.put("totalBruto", totalBruto);
        vars.put("totalDesconto", totalDesconto);
        vars.put("totalEfetivo", totalEfetivo);
        vars.put("originalTotal", originalTotal);
        vars.put("items", items);

        return reportRenderingService.render("atendimento/recibo", vars);
    }

    private List<BigDecimal> calculateProportional(List<BigDecimal> originalPrices, BigDecimal originalTotal, BigDecimal targetTotal) {
        if (originalPrices.isEmpty()) return List.of();

        List<BigDecimal> result = new ArrayList<>();
        BigDecimal assigned = BigDecimal.ZERO;

        if (originalTotal.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal each = originalPrices.size() == 1
                    ? targetTotal
                    : targetTotal.divide(new BigDecimal(originalPrices.size()), 2, RoundingMode.DOWN);
            for (int i = 0; i < originalPrices.size() - 1; i++) {
                result.add(each);
                assigned = assigned.add(each);
            }
            result.add(targetTotal.subtract(assigned));
            return result;
        }

        for (int i = 0; i < originalPrices.size() - 1; i++) {
            BigDecimal adjusted = originalPrices.get(i)
                    .multiply(targetTotal)
                    .divide(originalTotal, 2, RoundingMode.DOWN);
            result.add(adjusted);
            assigned = assigned.add(adjusted);
        }
        result.add(targetTotal.subtract(assigned));

        return result;
    }

    private BigDecimal resolveItemPrice(AtendimentoConsultaExame item, boolean hasCardPayment) {
        if (hasCardPayment) {
            BigDecimal card = item.getPriceCard();
            if (card != null && card.compareTo(BigDecimal.ZERO) != 0) return card;
        }
        return item.getPrice();
    }

    private String buildCombinedObservation(Company company, Atendimento atendimento) {
        String companyObs = (company != null && company.getObservacao() != null && !company.getObservacao().isBlank())
                ? company.getObservacao().trim() : null;
        String atendimentoObs = (atendimento.getObservacao() != null && !atendimento.getObservacao().isBlank())
                ? atendimento.getObservacao().trim() : null;

        if (companyObs != null && atendimentoObs != null) return companyObs + "\n\n" + atendimentoObs;
        if (companyObs != null) return companyObs;
        return atendimentoObs;
    }

    private Atendimento findOrThrow(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Atendimento não encontrado."));
    }
}
