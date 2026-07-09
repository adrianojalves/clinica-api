package br.com.ajasoftware.clinica.controller.log;

import br.com.ajasoftware.clinica.domain.dto.log.LogRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.log.LogResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.log.LogUpdateDTO;
import br.com.ajasoftware.clinica.domain.filter.log.LogFilter;
import br.com.ajasoftware.clinica.service.log.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/clinica/logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN')")
public class LogController {

    private final LogService service;

    @GetMapping
    public ResponseEntity<Page<LogResponseDTO>> list(
            LogFilter filter,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<LogResponseDTO> create(
            @RequestBody @Valid LogRequestDTO data,
            UriComponentsBuilder uriBuilder) {
        LogResponseDTO response = service.create(data);
        var uri = uriBuilder
                .path("/api/clinica/logs/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LogResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid LogUpdateDTO data) {
        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
