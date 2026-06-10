package br.com.ajasoftware.clinica.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main security configuration class for the application.
 * Disables CSRF, sets session to stateless, and configures route permissions.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Allows us to use @PreAuthorize in our controllers
@RequiredArgsConstructor
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 1. PUBLIC ENDPOINTS (API)
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clinica/company/logo").permitAll()
                        .requestMatchers("/error").permitAll()

                        // 2. SWAGGER / OPENAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // 3. FRONTEND STATIC RESOURCES & FONTS
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/*.js",
                                "/*.css",
                                "/*.ico",
                                "/*.png",
                                "/*.jpg",      // Added for logo.jpg
                                "/*.jpeg",
                                "/*.svg",      // Added for SVG icons
                                "/**.woff",    // Fonts in the root
                                "/**.woff2",
                                "/**.ttf",
                                "/**.eot",     // Added for font compatibility
                                "/assets/**",
                                "/images/**",
                                "/media/**"    // Allows everything inside media (fonts and assets)
                        ).permitAll()

                        // 4. ANGULAR SPA ROUTES
                        // GET to any non-API path is allowed: the browser needs to load
                        // index.html so Angular can boot and its authGuard protects the route.
                        // Real data security stays on /api/** via @PreAuthorize on each controller.
                        .requestMatchers((HttpServletRequest req) ->
                                HttpMethod.GET.name().equals(req.getMethod())
                                && !req.getServletPath().startsWith("/api/")
                        ).permitAll()

                        // 5. PROTECT EVERYTHING ELSE
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Exposes the AuthenticationManager so we can inject it into our AuthController later.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Defines BCrypt as the password hashing algorithm for the entire system.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}