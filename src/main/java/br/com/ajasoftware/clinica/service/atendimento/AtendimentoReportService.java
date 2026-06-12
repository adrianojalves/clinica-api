package br.com.ajasoftware.clinica.service.atendimento;

import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.atendimento.Atendimento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoConsultaExame;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoPagamento;
import br.com.ajasoftware.clinica.domain.entity.atendimento.AtendimentoStatus;
import br.com.ajasoftware.clinica.domain.entity.company.Company;
import br.com.ajasoftware.clinica.repository.AtendimentoPagamentoRepository;
import br.com.ajasoftware.clinica.repository.AtendimentoRepository;
import br.com.ajasoftware.clinica.repository.CompanyRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AtendimentoReportService {

    private final AtendimentoRepository atendimentoRepository;
    private final AtendimentoPagamentoRepository pagamentoRepository;
    private final CompanyRepository companyRepository;
    private final TemplateEngine templateEngine;

    @Transactional(readOnly = true)
    public byte[] generateEncaminhamento(Long id) {
        Atendimento atendimento = findOrThrow(id);
        Company company = getCompany();
        String logoBase64 = loadLogoBase64(company);

        Context ctx = new Context(new Locale("pt", "BR"));
        ctx.setVariable("atendimento", atendimento);
        ctx.setVariable("company", company);
        ctx.setVariable("logoBase64", logoBase64);
        ctx.setVariable("printedAt", LocalDateTime.now());
        ctx.setVariable("isOrcamento", atendimento.getStatus() == AtendimentoStatus.ABERTO);
        ctx.setVariable("usuario", currentUser());
        ctx.setVariable("observacaoCompleta", buildCombinedObservation(company, atendimento));

        // Trigger lazy collections inside the transaction
        atendimento.getItens().forEach(item -> {
            if (item.getMedicalProcedure() != null) item.getMedicalProcedure().getName();
            if (item.getDoctor() != null) item.getDoctor().getName();
        });
        if (atendimento.getCliente() != null) atendimento.getCliente().getName();
        if (atendimento.getClinica() != null) atendimento.getClinica().getName();

        String html = templateEngine.process("atendimento/encaminhamento", ctx);
        return renderPdf(html);
    }

    @Transactional(readOnly = true)
    public byte[] generateRecibo(Long id) {
        Atendimento atendimento = findOrThrow(id);
        List<AtendimentoPagamento> pagamentos = pagamentoRepository.findByAtendimentoId(id);
        boolean hasCardPayment = pagamentos.stream()
                .anyMatch(p -> p.getTipoPagamento().isCartao());

        BigDecimal total = atendimento.getItens().stream()
                .map(item -> resolveItemPrice(item, hasCardPayment))
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Company company = getCompany();
        String logoBase64 = loadLogoBase64(company);

        Context ctx = new Context(new Locale("pt", "BR"));
        ctx.setVariable("atendimento", atendimento);
        ctx.setVariable("pagamentos", pagamentos);
        ctx.setVariable("hasCardPayment", hasCardPayment);
        ctx.setVariable("total", total);
        ctx.setVariable("company", company);
        ctx.setVariable("logoBase64", logoBase64);
        ctx.setVariable("printedAt", LocalDateTime.now());
        ctx.setVariable("usuario", currentUser());

        // Trigger lazy collections inside the transaction
        atendimento.getItens().forEach(item -> {
            if (item.getMedicalProcedure() != null) item.getMedicalProcedure().getName();
        });
        if (atendimento.getCliente() != null) {
            atendimento.getCliente().getName();
            atendimento.getCliente().getCpf();
        }

        String html = templateEngine.process("atendimento/recibo", ctx);
        return renderPdf(html);
    }

    private BigDecimal resolveItemPrice(AtendimentoConsultaExame item, boolean hasCardPayment) {
        if (hasCardPayment) {
            BigDecimal card = item.getPriceCard();
            if (card != null && card.compareTo(BigDecimal.ZERO) != 0) return card;
        }
        return item.getPrice();
    }

    private User currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof User u ? u : null;
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao gerar PDF: " + e.getMessage());
        }
    }

    private Atendimento findOrThrow(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Atendimento não encontrado."));
    }

    private Company getCompany() {
        return companyRepository.findAll().stream().findFirst().orElse(null);
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

    private String loadLogoBase64(Company company) {
        if (company == null || company.getLogoUrl() == null) return null;
        // logoUrl is /logos/filename.ext → file is at uploads/logos/filename.ext
        String urlPath = company.getLogoUrl();
        String filePath = "uploads" + urlPath;
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) return null;
            byte[] bytes = Files.readAllBytes(path);
            String ext = urlPath.substring(urlPath.lastIndexOf('.') + 1).toLowerCase();
            String mimeType = "image/" + ("jpg".equals(ext) ? "jpeg" : ext);
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}
