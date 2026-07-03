package br.com.ajasoftware.clinica.service.security;

import br.com.ajasoftware.clinica.domain.entity.Role;
import br.com.ajasoftware.clinica.domain.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service responsible for creating and validating JSON Web Tokens (JWT).
 * Implements the Access + Refresh Token pattern for enhanced security.
 */
@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private static final String ISSUER = "clinica-api";

    /**
     * Generates a short-lived JWT access token (30 minutes).
     * Contains user roles to prevent unnecessary database queries.
     *
     * @param user The authenticated user object.
     * @return String representing the Access Token.
     */
    public String generateAccessToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getLogin())
                    .withClaim("id", user.getId())
                    .withClaim("roles", user.getRoles().stream().map(Role::getName).toList())
                    .withClaim("type", "access")
                    .withExpiresAt(generateExpirationDate(30)) // Expires in 30 minutes
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro interno: Não foi possível gerar o token de acesso.", exception);
        }
    }

    /**
     * Generates a long-lived JWT refresh token (7 days).
     * Used exclusively to request new access tokens. Contains no roles.
     *
     * @param user The authenticated user object.
     * @return String representing the Refresh Token.
     */
    public String generateRefreshToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getLogin())
                    .withClaim("type", "refresh") // Crucial: tags this as a refresh token
                    .withExpiresAt(generateExpirationDate(60 * 24 * 7)) // Expires in 7 days
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro interno: Não foi possível gerar o token de atualização.", exception);
        }
    }

    /**
     * Validates an ACCESS token and extracts the subject (login).
     * It strictly requires the token to have the claim type = "access".
     *
     * @param token The JWT token provided by the client header.
     * @return String representing the user login if valid, or an empty string if invalid.
     */
    public String validateAccessTokenAndGetSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "access") // Blocks refresh tokens from being used as access tokens
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (TokenExpiredException exception) {
            // Rethrow expiration specifically so the Filter can respond with 401
            throw exception;

        }
        catch (JWTVerificationException exception) {
            return "";
        }
    }

    /**
     * Validates a REFRESH token and extracts the subject (login).
     * It strictly requires the token to have the claim type = "refresh".
     *
     * @param token The JWT token provided by the client cookie.
     * @return String representing the user login if valid, or an empty string if invalid.
     */
    public String validateRefreshTokenAndGetSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "refresh")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException exception) {
            // Invalid or expired refresh token — throw so the controller can signal the frontend to re-authenticate
            throw new RuntimeException("Sessão expirada ou token de atualização inválido. Por favor, faça login novamente.");
        }
    }

    /**
     * Calculates the expiration date based on the provided minutes.
     * Calculated in UTC-independent Instant format.
     */
    private Instant generateExpirationDate(int minutesToAdd) {
        return Instant.now().plus(minutesToAdd, ChronoUnit.MINUTES);
    }
}