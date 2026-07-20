package br.com.ajasoftware.clinica.controller.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoDiarioReportFilter;
import br.com.ajasoftware.clinica.service.relatorio.atendimento.AtendimentoDiarioReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinica/atendimentos/relatorios/atendimento-diario")
@RequiredArgsConstructor
public class AtendimentoDiarioRelatorioController {

    private final AtendimentoDiarioReportService reportService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> generate(@Valid AtendimentoDiarioReportFilter filter) {
        byte[] pdf = reportService.generate(filter);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-atendimento-diario.pdf")
                .body(pdf);
    }
}
