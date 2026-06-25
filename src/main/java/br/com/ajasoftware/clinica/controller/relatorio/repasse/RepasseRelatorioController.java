package br.com.ajasoftware.clinica.controller.relatorio.repasse;

import br.com.ajasoftware.clinica.domain.dto.relatorio.repasse.RepasseReportFilter;
import br.com.ajasoftware.clinica.service.relatorio.repasse.RepasseReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinica/repasse/relatorios")
@RequiredArgsConstructor
public class RepasseRelatorioController {

    private final RepasseReportService repasseReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RELATORIOS')")
    public ResponseEntity<byte[]> generate(RepasseReportFilter filter) {
        byte[] pdf = repasseReportService.generate(filter);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-repasse.pdf")
                .body(pdf);
    }
}
