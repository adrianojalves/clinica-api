package br.com.ajasoftware.clinica.controller.users;

import br.com.ajasoftware.clinica.domain.dto.users.RoleResponseDTO;
import br.com.ajasoftware.clinica.service.users.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clinica/role")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponseDTO> listRoles() {
        return roleService.listAll();
    }
}
