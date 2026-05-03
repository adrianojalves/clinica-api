package br.com.ajasoftware.clinica.controller.clinics;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicStatusDTO;
import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicUpdateDTO;
import br.com.ajasoftware.clinica.domain.filter.clinics.ClinicFilter;
import br.com.ajasoftware.clinica.service.clinics.ClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST Controller for managing clinics.
 */
@RestController
@RequestMapping("/api/clinica/clinics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
public class ClinicController {

    private final ClinicService clinicService;

    /**
     * Endpoint to list clinics with pagination and dynamic filtering (name, cnpj).
     * Example: GET /api/clinica/clinics?name=Saude&page=0
     */
    @GetMapping
    public ResponseEntity<Page<ClinicResponseDTO>> listClinics(
            ClinicFilter filter,
            @PageableDefault(size = 10, sort = {"name"}) Pageable pageable) {

        var page = clinicService.listAll(filter, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Endpoint to check if a CNPJ is already registered.
     * Example: GET /api/clinica/clinics/exists?cnpj=12345678000199
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkCnpjExists(@RequestParam String cnpj) {
        boolean exists = clinicService.checkCnpjExists(cnpj);
        return ResponseEntity.ok(exists);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestBody @Valid ClinicStatusDTO data) {
        clinicService.updateStatus(id, data.active());
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to create a new clinic.
     * Example: POST /api/clinica/clinics
     */
    @PostMapping
    public ResponseEntity<ClinicResponseDTO> createClinic(
            @RequestBody @Valid ClinicRequestDTO data,
            UriComponentsBuilder uriBuilder) {

        ClinicResponseDTO clinic = clinicService.create(data);

        var uri = uriBuilder.path("/api/clinica/clinics/{id}").buildAndExpand(clinic.id()).toUri();

        return ResponseEntity.created(uri).body(clinic);
    }

    /**
     * Endpoint to update an existing clinic.
     * Example: PUT /api/clinica/clinics/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClinicResponseDTO> updateClinic(
            @PathVariable Long id,
            @RequestBody @Valid ClinicUpdateDTO data) {

        ClinicResponseDTO clinic = clinicService.update(id, data);

        return ResponseEntity.ok(clinic);
    }
}