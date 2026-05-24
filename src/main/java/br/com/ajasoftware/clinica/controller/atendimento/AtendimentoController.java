package br.com.ajasoftware.clinica.controller.atendimento;

import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.atendimento.AtendimentoResponseDTO;
import br.com.ajasoftware.clinica.domain.filter.atendimento.AtendimentoFilter;
import br.com.ajasoftware.clinica.service.atendimento.AtendimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/clinica/atendimentos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
public class AtendimentoController {

    private final AtendimentoService service;

    @GetMapping
    public ResponseEntity<Page<AtendimentoResponseDTO>> list(
            AtendimentoFilter filter,
            @PageableDefault(size = 10, sort = "dataEmissao") Pageable pageable) {

        return ResponseEntity.ok(service.list(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtendimentoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
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
    public ResponseEntity<AtendimentoResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid AtendimentoRequestDTO data) {

        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
