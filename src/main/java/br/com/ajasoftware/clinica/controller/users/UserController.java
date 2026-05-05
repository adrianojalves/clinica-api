package br.com.ajasoftware.clinica.controller.users;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserStatusDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserUpdateRequestDTO;
import br.com.ajasoftware.clinica.domain.filter.users.UserFilter;
import br.com.ajasoftware.clinica.service.users.UserService;
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
 * REST Controller for User management.
 * All endpoints require the client to be authenticated and have the ADMIN role.
 */
@RestController
@RequestMapping("/api/clinica/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * Endpoint to list active users with dynamic filtering and pagination.
     * Example request: GET /api/clinica/users?name=joao&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> listUsers(
            @ModelAttribute UserFilter filter,
            @PageableDefault(size = 10, sort = {"name"}) Pageable pageable) {

        var page = userService.listAll(filter, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Endpoint to logically delete (deactivate) a user.
     * Example request: DELETE /api/clinica/users/1
     */
    /**
     * Endpoint to partially update a user's status (Activate/Deactivate).
     * Example request: PATCH /api/clinica/users/1/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @RequestBody @Valid UserStatusDTO data) {

        userService.updateStatus(id, data.active());

        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO data,
                                                      UriComponentsBuilder uriBuilder) {

        var responseDTO = userService.createUser(data);

        var uri = uriBuilder.path("/api/clinica/users/{id}").buildAndExpand(responseDTO.id()).toUri();

        return ResponseEntity.created(uri).body(responseDTO);
    }

    /**
     * Endpoint to check if a login is already in use.
     * Ideal for asynchronous frontend validation (e.g., onBlur events).
     * Example request: GET /api/clinica/users/exists?login=joao.silva
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkLoginExists(@RequestParam String login) {
        boolean exists = userService.checkLoginExists(login);

        return ResponseEntity.ok(exists);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @RequestBody @Valid UserUpdateRequestDTO data) {

        var responseDTO = userService.updateUser(id, data);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}