package br.com.ajasoftware.clinica.controller.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoPagamentoRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoPagamentoResponseDTO;
import br.com.ajasoftware.clinica.service.atendimento.AtendimentoPagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/clinica/atendimentos/{atendimentoId}/pagamentos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
public class AtendimentoPagamentoController {

    private final AtendimentoPagamentoService service;

    @GetMapping
    public ResponseEntity<List<AtendimentoPagamentoResponseDTO>> list(@PathVariable Long atendimentoId) {
        return ResponseEntity.ok(service.listByAtendimento(atendimentoId));
    }

    @PostMapping
    public ResponseEntity<AtendimentoPagamentoResponseDTO> create(
            @PathVariable Long atendimentoId,
            @RequestBody @Valid AtendimentoPagamentoRequestDTO data,
            UriComponentsBuilder uriBuilder) {

        AtendimentoPagamentoResponseDTO response = service.create(atendimentoId, data);
        var uri = uriBuilder
                .path("/api/clinica/atendimentos/{atendimentoId}/pagamentos/{id}")
                .buildAndExpand(atendimentoId, response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtendimentoPagamentoResponseDTO> update(
            @PathVariable Long atendimentoId,
            @PathVariable Long id,
            @RequestBody @Valid AtendimentoPagamentoRequestDTO data) {

        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<Void> delete(@PathVariable Long atendimentoId, @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
