package br.com.ajasoftware.clinica.controller.security;

import br.com.ajasoftware.clinica.domain.dto.security.AuthenticationRequestDTO;
import br.com.ajasoftware.clinica.domain.dto.security.AuthenticationResponseDTO;
import br.com.ajasoftware.clinica.domain.entity.User;
import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.security.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Value("${api.security.cookie.secure}")
    private boolean isCookieSecure;

    /**
     * Authenticates the user and generates both Access and Refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody @Valid AuthenticationRequestDTO data,
                                                           HttpServletResponse response) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());

        var auth = this.authenticationManager.authenticate(usernamePassword);

        var user = (User) auth.getPrincipal();

        var accessToken = tokenService.generateAccessToken(user);
        var refreshToken = tokenService.generateRefreshToken(user);

        addRefreshTokenCookie(response, refreshToken);

        return ResponseEntity.ok(new AuthenticationResponseDTO(accessToken));
    }

    /**
     * Generates a new Access Token using a valid Refresh Token from the cookie.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDTO> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                             HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Sessão expirada. Por favor, faça login novamente.");
        }

        var login = tokenService.validateRefreshTokenAndGetSubject(refreshToken);

        var user = userRepository.findByLoginWithRoles(login)
                .orElseThrow(() -> new RuntimeException("Erro: Usuário não encontrado no sistema."));

        var newAccessToken = tokenService.generateAccessToken(user);
        var newRefreshToken = tokenService.generateRefreshToken(user);

        addRefreshTokenCookie(response, newRefreshToken);

        return ResponseEntity.ok(new AuthenticationResponseDTO(newAccessToken));
    }

    /**
     * Helper method to build and attach the HttpOnly cookie.
     */
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                // In production with HTTPS, set secure(true). For local dev, false.
                .secure(isCookieSecure)
                // Restricts the browser to ONLY send this cookie when hitting the refresh endpoint
                .path("/api/auth/refresh")
                // Matches the 7 days expiration of the token
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}