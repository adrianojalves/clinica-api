package br.com.ajasoftware.clinica.controller.relatorio.historico;

import br.com.ajasoftware.clinica.domain.dto.relatorio.historico.HistoricoPacienteReportFilter;
import br.com.ajasoftware.clinica.service.relatorio.historico.HistoricoPacienteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinica/pacientes/relatorios/historico")
@RequiredArgsConstructor
public class HistoricoPacienteRelatorioController {

    private final HistoricoPacienteReportService historicoPacienteReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RELATORIOS')")
    public ResponseEntity<byte[]> generate(HistoricoPacienteReportFilter filter) {
        byte[] pdf = historicoPacienteReportService.generate(filter);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-historico-paciente.pdf")
                .body(pdf);
    }
}
