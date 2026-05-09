package br.com.ajasoftware.clinica.service.users;

import br.com.ajasoftware.clinica.domain.dto.users.RoleResponseDTO;
import br.com.ajasoftware.clinica.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponseDTO> listAll() {
        return roleRepository.findAll().stream().map(RoleResponseDTO::new).toList();
    }
}
