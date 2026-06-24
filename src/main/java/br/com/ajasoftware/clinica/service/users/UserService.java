package br.com.ajasoftware.clinica.service.users;

import br.com.ajasoftware.clinica.domain.dto.clinics.ClinicResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserSummaryDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserUpdateRequestDTO;
import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.domain.entity.clinics.Clinic;
import br.com.ajasoftware.clinica.domain.filter.users.UserFilter;
import br.com.ajasoftware.clinica.repository.RoleRepository;
import br.com.ajasoftware.clinica.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;

/**
 * Service responsible for the business logic regarding User management.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves a paginated list of active users based on dynamic filters.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listAll(UserFilter filter, Pageable pageable) {
        return userRepository.findWithFilter(filter, pageable).map(UserResponseDTO::new);
    }

    /**
     * Updates the user's active status.
     * Can be used to deactivate (Soft Delete) or reactivate a user.
     */
    @Transactional
    public void updateStatus(Long id, Boolean active) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operação falhou: Usuário não encontrado no sistema."));

        user.setActive(active);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO data) {
        if (userRepository.existsByLogin(data.login())) {
            throw new IllegalArgumentException("Operação falhou: O login informado já está em uso.");
        }

        // 2. Fetch the roles from the database
        var roles = roleRepository.findAllById(data.roleIds());
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Operação falhou: Nenhum perfil de acesso válido foi encontrado.");
        }

        // 3. Map DTO to Entity
        var user = new User();
        user.setName(data.name());
        user.setLogin(data.login());
        user.setPassword(passwordEncoder.encode(data.password()));
        user.setEmail(data.email());
        user.setPhone(data.phone());
        user.setActive(true); // New users are active by default
        user.setPercentutalDesconto(data.percentualDesconto());
        user.setRoles(new HashSet<>(roles));

        userRepository.save(user);

        return new UserResponseDTO(user);
    }

    /**
     * Checks if a given login is already registered in the database.
     *
     * @param login The login string to check.
     * @return true if the login exists, false otherwise.
     */
    public boolean checkLoginExists(String login) {
        if (login == null || login.isBlank()) {
            return false;
        }
        return userRepository.existsByLogin(login);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO data) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operação falhou: Usuário não encontrado no sistema."));

        var roles = roleRepository.findAllById(data.roleIds());
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Operação falhou: Nenhum perfil de acesso válido foi encontrado.");
        }

        user.setName(data.name());
        user.setEmail(data.email());
        user.setPhone(data.phone());
        user.setActive(data.active());
        user.setPercentutalDesconto(data.percentualDesconto());
        user.setRoles(new HashSet<>(roles));

        if (data.password() != null && !data.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(data.password()));
        }

        return new UserResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryDTO> listSummary(UserFilter filter, Pageable pageable) {
        return userRepository.findSummaryWithFilter(filter, pageable);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getById(Long id) {
        User user = findEntityById(id);
        return new UserResponseDTO(user);
    }

    private User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }
}