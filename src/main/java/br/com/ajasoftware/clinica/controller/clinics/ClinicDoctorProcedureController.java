package br.com.ajasoftware.clinica.controller.clinics;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureFilterDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicDoctorProcedureResponseDTO;
import br.com.ajasoftware.clinica.service.clinics.ClinicDoctorProcedureService;
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
@RequestMapping("/api/clinica/clinic-procedures")
@RequiredArgsConstructor
public class ClinicDoctorProcedureController {

    private final ClinicDoctorProcedureService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS', 'ATENDIMENTO')")
    public ResponseEntity<Page<ClinicDoctorProcedureResponseDTO>> list(
            ClinicDoctorProcedureFilterDTO filter,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(service.listWithFilters(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<ClinicDoctorProcedureResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<ClinicDoctorProcedureResponseDTO> create(
            @RequestBody @Valid ClinicDoctorProcedureRequestDTO data,
            UriComponentsBuilder uriBuilder) {

        ClinicDoctorProcedureResponseDTO response = service.create(data);
        var uri = uriBuilder.path("/api/clinica/clinic-procedures/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<ClinicDoctorProcedureResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ClinicDoctorProcedureRequestDTO data) {

        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}