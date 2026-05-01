package br.com.ajasoftware.clinica.service.users;

import br.com.ajasoftware.clinica.domain.dto.users.UserRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserResponseDTO;
import br.com.ajasoftware.clinica.domain.dto.users.UserUpdateRequestDTO;
import br.com.ajasoftware.clinica.domain.entity.Role;
import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.repository.RoleRepository;
import br.com.ajasoftware.clinica.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService using Mockito to isolate the database.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role(1L, "ROLE_ADMIN");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("UserName UserLastName");
        mockUser.setLogin("user.admin");
        mockUser.setPassword("hashed_password");
        mockUser.setEmail("user@aadmin.com.br");
        mockUser.setActive(true);
        mockUser.setRoles(new HashSet<>(Set.of(mockRole)));
    }

    @Test
    @DisplayName("Should create a user successfully and encode the password")
    void createUserSuccessfully() {
        // Arrange
        var requestDTO = new UserRequestDTO("UserName UserLastName", "user.admin", "123456", "user@admin.com.br", "11999999999", List.of(1L));

        when(userRepository.existsByLogin("user.admin")).thenReturn(false);
        when(roleRepository.findAllById(List.of(1L))).thenReturn(List.of(mockRole));
        when(passwordEncoder.encode("123456")).thenReturn("hashed_123456");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        UserResponseDTO response = userService.createUser(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals("UserName UserLastName", response.name());
        verify(passwordEncoder, times(1)).encode("123456"); // Verifies if the encoder was called
        verify(userRepository, times(1)).save(any(User.class)); // Verifies if it was saved
    }

    @Test
    @DisplayName("Should throw exception when trying to create user with existing login")
    void throwExceptionWhenLoginExists() {
        // Arrange
        var requestDTO = new UserRequestDTO("UserName", "user.admin", "123456", "test@test.com", null, List.of(1L));
        when(userRepository.existsByLogin("user.admin")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(requestDTO);
        });

        assertTrue(exception.getMessage().contains("já está em uso"));
        verify(userRepository, never()).save(any(User.class)); // Ensures save was NEVER called
    }

    @Test
    @DisplayName("Should logically deactivate a user (Soft Delete)")
    void deactivateUserSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        userService.deactivateUser(1L);

        // Assert
        assertFalse(mockUser.isActive(), "The user should be set to inactive");
    }

    @Test
    @DisplayName("Should update user and encode new password if provided")
    void updateUserWithNewPassword() {
        // Arrange
        var updateDTO = new UserUpdateRequestDTO("UserName Edited", "novo@email.com", null, true, "novaSenha123", List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findAllById(List.of(1L))).thenReturn(List.of(mockRole));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new_hashed_password");

        // Act
        UserResponseDTO response = userService.updateUser(1L, updateDTO);

        // Assert
        assertEquals("UserName Edited", response.name());
        assertEquals("new_hashed_password", mockUser.getPassword()); // Verifies the entity in memory was updated
        verify(passwordEncoder, times(1)).encode("novaSenha123");
    }

    @Test
    @DisplayName("Should update user WITHOUT changing password if password is null or blank")
    void updateUserWithoutChangingPassword() {
        // Arrange
        var updateDTO = new UserUpdateRequestDTO("UserName Editado", "novo@email.com", null, true, "", List.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findAllById(List.of(1L))).thenReturn(List.of(mockRole));

        // Act
        userService.updateUser(1L, updateDTO);

        // Assert
        assertEquals("hashed_password", mockUser.getPassword()); // The password should remain the original one
        verify(passwordEncoder, never()).encode(anyString()); // Encoder should NEVER be called
    }
}