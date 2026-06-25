package br.com.ajasoftware.clinica.controller.relatorio.abc;

import br.com.ajasoftware.clinica.domain.dto.relatorio.abc.AbcReportFilter;
import br.com.ajasoftware.clinica.service.relatorio.abc.AbcProcedimentoReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinica/procedimentos/relatorios/abc")
@RequiredArgsConstructor
public class AbcProcedimentoRelatorioController {

    private final AbcProcedimentoReportService abcReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RELATORIOS')")
    public ResponseEntity<byte[]> generate(AbcReportFilter filter) {
        byte[] pdf = abcReportService.generate(filter);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-abc-procedimentos.pdf")
                .body(pdf);
    }
}
