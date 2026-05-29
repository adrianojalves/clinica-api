package br.com.ajasoftware.clinica.controller.client;

import br.com.ajasoftware.clinica.domain.dto.client.ClientRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.client.ClientResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.client.ClientUpdateDTO;
import br.com.ajasoftware.clinica.domain.dto.doctors.DoctorStatusDTO;
import br.com.ajasoftware.clinica.domain.filter.client.ClientFilter;
import br.com.ajasoftware.clinica.service.client.ClientService;
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
@RequestMapping("/api/clinica/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Accessible by any authenticated user (Read operations).
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ClientResponseDTO>> list(
            ClientFilter filter,
            @PageableDefault(sort = {"name"}) Pageable pageable) {
        return ResponseEntity.ok(clientService.listWithFilter(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<ClientResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    /**
     * Write operations restricted to ADMIN or CADASTROS roles.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<ClientResponseDTO> create(
            @RequestBody @Valid ClientRequestDTO data,
            UriComponentsBuilder uriBuilder) {

        ClientResponseDTO response = clientService.create(data);
        var uri = uriBuilder.path("/api/clinica/clients/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<ClientResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ClientUpdateDTO data) {

        ClientResponseDTO response = clientService.update(id, data);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long id,
            @RequestBody @Valid DoctorStatusDTO data) {

        clientService.changeStatus(id, data.active());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-cpf/{cpf}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CADASTROS')")
    public ResponseEntity<ClientResponseDTO> getByCpf(@PathVariable String cpf) {
        return clientService.findByCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}