package br.com.ajasoftware.clinica.controller.medical.procedures;

import br.com.ajasoftware.clinica.domain.dto.medical.procedures.ProcedureRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.medical.procedures.ProcedureResponseDTO;
import br.com.ajasoftware.clinica.domain.filter.medical.procedure.ProcedureFilterDTO;
import br.com.ajasoftware.clinica.service.medical.procedures.MedicalProcedureService;
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
@RequestMapping("/api/clinica/procedures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
public class MedicalProcedureController {

    private final MedicalProcedureService service;

    /**
     * GET /api/clinica/procedures?name=Raio&type=EXAME&page=0
     */
    @GetMapping
    public ResponseEntity<Page<ProcedureResponseDTO>> list(
            ProcedureFilterDTO filter,
            @PageableDefault(size = 10, sort = {"name"}) Pageable pageable) {

        Page<ProcedureResponseDTO> page = service.listWithFilters(filter, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcedureResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProcedureResponseDTO> create(
            @RequestBody @Valid ProcedureRequestDTO data,
            UriComponentsBuilder uriBuilder) {

        ProcedureResponseDTO response = service.create(data);
        var uri = uriBuilder.path("/api/clinica/procedures/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcedureResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ProcedureRequestDTO data) {

        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}