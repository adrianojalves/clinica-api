package br.com.ajasoftware.clinica.controller.doctors;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorStatusDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorUpdateDTO;
import br.com.ajasoftware.clinica.domain.filter.doctors.DoctorFilter;
import br.com.ajasoftware.clinica.service.doctors.DoctorService;
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
@RequestMapping("/api/clinica/doctors")
@RequiredArgsConstructor // Lombok generates the constructor injecting DoctorService automatically
@PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<Page<DoctorResponseDTO>> list(
            DoctorFilter filter,
            @PageableDefault(size = 10, sort = {"name"}) Pageable pageable) {

        Page<DoctorResponseDTO> page = doctorService.listWithFilter(filter, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> create(
            @RequestBody @Valid DoctorRequestDTO data,
            UriComponentsBuilder uriBuilder) {

        DoctorResponseDTO response = doctorService.create(data);
        var uri = uriBuilder.path("/api/clinica/doctors/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid DoctorUpdateDTO data) {

        DoctorResponseDTO response = doctorService.update(id, data);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to change the active status of a doctor (Soft Delete/Reactivate).
     * Example: PATCH /api/clinica/doctors/1/status
     * Body: { "active": false }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestBody @Valid DoctorStatusDTO data) {

        doctorService.changeStatus(id, data.active());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }
}