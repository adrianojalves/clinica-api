package br.com.ajasoftware.clinica.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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

                        // 2. FRONTEND STATIC RESOURCES & FONTS
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/*.js",
                                "/*.css",
                                "/*.ico",
                                "/*.png",
                                "/*.jpg",      // Adicionado para sua logo.jpg
                                "/*.jpeg",
                                "/*.svg",      // Adicionado para ícones em SVG
                                "/**.woff",    // Fontes na raiz
                                "/**.woff2",
                                "/**.ttf",
                                "/**.eot",     // Adicionado para compatibilidade de fontes
                                "/assets/**",
                                "/images/**",
                                "/media/**"    // Libera tudo dentro de media (fontes e assets)
                        ).permitAll()

                        // 3. PROTECT EVERYTHING ELSE
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/images/**",
                "/media/**",
                "/assets/**",
                "/favicon.ico",
                "/*.js",
                "/*.css"
        );
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