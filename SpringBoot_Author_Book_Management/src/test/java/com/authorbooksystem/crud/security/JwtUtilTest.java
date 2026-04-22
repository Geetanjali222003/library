package com.authorbooksystem.crud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String TEST_SECRET = "mysecretkeymysecretkeymysecretkey";
    private static final Key TEST_KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());

    @Test
    void generateToken_ShouldCreateValidJwtToken() {
        // Given
        String username = "testuser";
        String role = "ADMIN";

        // When
        String token = JwtUtil.generateToken(username, role);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // Verify token structure (should have 3 parts separated by dots)
        String[] tokenParts = token.split("\\.");
        assertThat(tokenParts).hasSize(3);
    }

    @Test
    void validateToken_ShouldReturnClaims_WhenValidToken() {
        // Given
        String username = "testuser";
        String role = "ADMIN";
        String token = JwtUtil.generateToken(username, role);

        // When
        Claims claims = JwtUtil.validateToken(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.get("role")).isEqualTo(role);
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    void generateToken_ShouldIncludeExpirationTime() {
        // Given
        String username = "testuser";
        String role = "USER";
        Date beforeGeneration = new Date();

        // When
        String token = JwtUtil.generateToken(username, role);
        Claims claims = JwtUtil.validateToken(token);

        // Then
        Date expiration = claims.getExpiration();
        Date expectedExpiration = new Date(beforeGeneration.getTime() + 3600000); // 1 hour
        
        // Allow for small time differences (within 1 second)
        long timeDiff = Math.abs(expiration.getTime() - expectedExpiration.getTime());
        assertThat(timeDiff).isLessThan(1000);
    }

    @Test
    void generateToken_ShouldIncludeIssuedAtTime() {
        // Given
        String username = "testuser";
        String role = "LIBRARIAN";

        // When
        String token = JwtUtil.generateToken(username, role);
        Claims claims = JwtUtil.validateToken(token);

        // Then
        Date issuedAt = claims.getIssuedAt();
        Date now = new Date();
        assertThat(issuedAt).isBeforeOrEqualTo(now);
        // Allow for reasonable time difference (within 5 seconds)
        assertThat(now.getTime() - issuedAt.getTime()).isLessThan(5000);
    }

    @Test
    void generateToken_ShouldHandleDifferentRoles() {
        // Given
        String[] roles = {"ADMIN", "USER", "LIBRARIAN"};
        String username = "testuser";

        for (String role : roles) {
            // When
            String token = JwtUtil.generateToken(username, role);
            Claims claims = JwtUtil.validateToken(token);

            // Then
            assertThat(claims.get("role")).isEqualTo(role);
            assertThat(claims.getSubject()).isEqualTo(username);
        }
    }

    @Test
    void generateToken_ShouldHandleDifferentUsernames() {
        // Given
        String[] usernames = {"admin", "user123", "test@email.com"};
        String role = "USER";

        for (String username : usernames) {
            // When
            String token = JwtUtil.generateToken(username, role);
            Claims claims = JwtUtil.validateToken(token);

            // Then
            assertThat(claims.getSubject()).isEqualTo(username);
            assertThat(claims.get("role")).isEqualTo(role);
        }
    }

    @Test
    void tokenRoundTrip_ShouldMaintainDataIntegrity() {
        // Given
        String originalUsername = "complexUser@domain.com";
        String originalRole = "SUPER_ADMIN";

        // When
        String token = JwtUtil.generateToken(originalUsername, originalRole);
        Claims retrievedClaims = JwtUtil.validateToken(token);

        // Then
        assertThat(retrievedClaims.getSubject()).isEqualTo(originalUsername);
        assertThat(retrievedClaims.get("role")).isEqualTo(originalRole);
    }
}
