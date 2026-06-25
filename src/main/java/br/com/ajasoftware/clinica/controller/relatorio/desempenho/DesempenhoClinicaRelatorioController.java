package br.com.ajasoftware.clinica.controller.relatorio.desempenho;

import br.com.ajasoftware.clinica.domain.dto.relatorio.desempenho.DesempenhoClinicaReportFilter;
import br.com.ajasoftware.clinica.service.relatorio.desempenho.DesempenhoClinicaReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinica/desempenho/relatorios")
@RequiredArgsConstructor
public class DesempenhoClinicaRelatorioController {

    private final DesempenhoClinicaReportService desempenhoReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RELATORIOS')")
    public ResponseEntity<byte[]> generate(DesempenhoClinicaReportFilter filter) {
        byte[] pdf = desempenhoReportService.generate(filter);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-desempenho-clinica.pdf")
                .body(pdf);
    }
}
