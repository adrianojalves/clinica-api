package br.com.ajasoftware.clinica.service.security;

import br.com.ajasoftware.clinica.domain.entity.Role;
import br.com.ajasoftware.clinica.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TokenService class.
 * Tests are executed without starting the full Spring Application Context for maximum speed.
 */
class TokenServiceTest {

    private TokenService tokenService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Instantiates the service manually
        tokenService = new TokenService();

        // Injects the secret variable manually bypassing the @Value annotation
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key-12345");

        // Creates a mock user for our tests
        Role adminRole = new Role(1L, "ROLE_ADMIN");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setLogin("test_user");
        mockUser.setRoles(Set.of(adminRole));
    }

    @Test
    @DisplayName("Should generate and validate an Access Token successfully")
    void generateAndValidateAccessTokenSuccessfully() {
        // Act
        String token = tokenService.generateAccessToken(mockUser);
        String subject = tokenService.validateAccessTokenAndGetSubject(token);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("test_user", subject);
    }

    @Test
    @DisplayName("Should generate and validate a Refresh Token successfully")
    void generateAndValidateRefreshTokenSuccessfully() {
        // Act
        String token = tokenService.generateRefreshToken(mockUser);
        String subject = tokenService.validateRefreshTokenAndGetSubject(token);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("test_user", subject);
    }

    @Test
    @DisplayName("Should return empty string when trying to validate a Refresh Token as an Access Token")
    void failWhenValidatingRefreshTokenAsAccessToken() {
        // Arrange: Generate a valid REFRESH token
        String refreshToken = tokenService.generateRefreshToken(mockUser);

        // Act: Try to validate it using the ACCESS token method
        String subject = tokenService.validateAccessTokenAndGetSubject(refreshToken);

        // Assert: The system must reject it (return empty string) because the "type" claim does not match
        assertTrue(subject.isEmpty(), "Subject should be empty because the token type is 'refresh', not 'access'");
    }
}