package br.com.ajasoftware.clinica.security;

import br.com.ajasoftware.clinica.repository.UserRepository;
import br.com.ajasoftware.clinica.service.security.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts all HTTP requests to validate the JWT Access Token.
 * It executes once per request.
 */
@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        var token = this.recoverToken(request);

        if (token != null) {
            var login = tokenService.validateAccessTokenAndGetSubject(token);

            if (!login.isEmpty()) {
                // If token is valid, we fetch the user and tell Spring they are authenticated
                var user = userRepository.findByLoginWithRoles(login)
                        .orElseThrow(() -> new RuntimeException("Erro de autenticação: Usuário não encontrado."));

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continue the filter chain (let the request proceed to the Controller or be blocked by Spring)
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the token from the standard "Authorization: Bearer <token>" header.
     */
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}