package br.com.ajasoftware.clinica.controller.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO;
import br.com.ajasoftware.clinica.domain.filter.atendimento.AtendimentoFilter;
import br.com.ajasoftware.clinica.service.atendimento.AtendimentoReportService;
import br.com.ajasoftware.clinica.service.atendimento.AtendimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/clinica/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService service;
    private final AtendimentoReportService reportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<Page<AtendimentoResponseDTO>> list(
            AtendimentoFilter filter,
            @PageableDefault(size = 10)
            @SortDefault.SortDefaults({
                @SortDefault(sort = "status", direction = Sort.Direction.ASC),
                @SortDefault(sort = "id", direction = Sort.Direction.DESC)
            }) Pageable pageable) {

        return ResponseEntity.ok(service.list(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<AtendimentoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<AtendimentoResponseDTO> create(
            @RequestBody @Valid AtendimentoRequestDTO data,
            UriComponentsBuilder uriBuilder) {

        AtendimentoResponseDTO response = service.create(data);
        var uri = uriBuilder
                .path("/api/clinica/atendimentos/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<AtendimentoResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid AtendimentoRequestDTO data) {

        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<AtendimentoResponseDTO> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(service.finalizar(id));
    }

    @GetMapping("/{id}/encaminhamento")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<byte[]> encaminhamento(@PathVariable Long id) {
        byte[] pdf = reportService.generateEncaminhamento(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=encaminhamento-" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/{id}/recibo")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<byte[]> recibo(@PathVariable Long id) {
        byte[] pdf = reportService.generateRecibo(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recibo-" + id + ".pdf")
                .body(pdf);
    }
}
