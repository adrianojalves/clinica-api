package br.com.ajasoftware.clinica.service.relatorio;

import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.company.Company;
import br.com.ajasoftware.clinica.repository.CompanyRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportRenderingService {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private final CompanyRepository companyRepository;
    private final TemplateEngine templateEngine;

    public byte[] render(String templateName, Map<String, Object> variables) {
        Company company = getCompany();
        String logoBase64 = loadLogoBase64(company);

        Context ctx = new Context(PT_BR);
        ctx.setVariable("company", company);
        ctx.setVariable("logoBase64", logoBase64);
        ctx.setVariable("printedAt", LocalDateTime.now());
        ctx.setVariable("usuario", currentUser());
        variables.forEach(ctx::setVariable);

        String html = templateEngine.process(templateName, ctx);
        return renderPdf(html);
    }

    public Company getCompany() {
        return companyRepository.findAll().stream().findFirst().orElse(null);
    }

    public User currentUser() {
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

    private String loadLogoBase64(Company company) {
        if (company == null || company.getLogoUrl() == null) return null;
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
