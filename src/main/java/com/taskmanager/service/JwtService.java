package com.taskmanager.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service responsável por gerar e validar tokens JWT.
 *
 * ATUALIZADO PARA: jjwt 0.13.0
 *
 * Mudanças da API:
 * - setSubject() → subject()
 * - setIssuedAt() → issuedAt()
 * - setExpiration() → expiration()
 * - setSigningKey() → verifyWith()
 * - parseClaimsJws() → parseSignedClaims()
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Gera um token JWT a partir da autenticação.
     *
     * API MODERNA (jjwt 0.13.0):
     * - subject() em vez de setSubject()
     * - issuedAt() em vez de setIssuedAt()
     * - expiration() em vez de setExpiration()
     * - signWith(key) em vez de signWith(key, algorithm)
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // Cria a chave secreta
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        /**
         * API MODERNA (0.13.0)
         *
         * Mudanças:
         * - builder() continua igual
         * - subject() → método moderno (não deprecated)
         * - issuedAt() → método moderno
         * - expiration() → método moderno
         * - signWith(key) → algoritmo é inferido automaticamente da chave
         * - compact() continua igual
         */
        assert userDetails != null;
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Extrai o username do token JWT.
     *
     * API MODERNA (jjwt 0.13.0):
     * - parserBuilder() continua igual
     * - verifyWith(key) em vez de setSigningKey(key)
     * - parseSignedClaims() em vez de parseClaimsJws()
     * - payload() em vez de getBody()
     * - getSubject() continua igual
     */
    public String getUsernameFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        /**
         * API MODERNA (0.13.0)
         *
         * Mudanças:
         * - parserBuilder() → continua
         * - verifyWith(key) → novo método (não setSigningKey)
         * - build() → continua
         * - parseSignedClaims(token) → novo método (não parseClaimsJws)
         * - payload() → novo método (não getBody)
         * - getSubject() → continua
         */
        Claims claims = Jwts.parser()                        // ✅ Novo parser (não parserBuilder)
                .verifyWith(key)                             // ✅ Novo método
                .build()
                .parseSignedClaims(token)                    // ✅ Novo método
                .getPayload();                               // ✅ Novo método

        return claims.getSubject();
    }

    /**
     * Valida se o token JWT é válido.
     *
     * API MODERNA (jjwt 0.13.0):
     * - Mesmas mudanças do método acima
     * - Exceções continuam iguais
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            /**
             * API MODERNA (0.13.0)
             *
             * Se conseguir parsear sem exceção → token válido
             */
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException ex) {
            // JwtException é a classe pai de todas as exceções JWT
            System.err.println("Token JWT inválido: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("Token JWT vazio: " + ex.getMessage());
        }

        return false;
    }
}