package br.com.ajasoftware.clinica.controller.relatorio.atendimento;

import br.com.ajasoftware.clinica.domain.dto.relatorio.atendimento.AtendimentoReportFilter;
import br.com.ajasoftware.clinica.service.relatorio.atendimento.AtendimentoFinancialReportService;
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
@RequestMapping("/api/clinica/atendimentos/relatorios")
@RequiredArgsConstructor
public class AtendimentoRelatorioController {

    private final AtendimentoFinancialReportService financialReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RELATORIOS')")
    public ResponseEntity<byte[]> generate(@Valid AtendimentoReportFilter filter) {
        byte[] pdf = financialReportService.generate(filter);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=relatorio-atendimentos-" + filter.getTipo().name().toLowerCase() + ".pdf")
                .body(pdf);
    }
}
