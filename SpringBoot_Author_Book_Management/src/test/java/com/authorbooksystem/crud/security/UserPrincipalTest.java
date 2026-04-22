package com.authorbooksystem.crud.security;

import com.authorbooksystem.crud.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    private UserPrincipal userPrincipal;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ADMIN");

        userPrincipal = new UserPrincipal(testUser);
    }

    @Test
    void getAuthorities_ShouldReturnCorrectRole() {
        // When
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        // Then
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void getPassword_ShouldReturnUserPassword() {
        // When & Then
        assertThat(userPrincipal.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void getUsername_ShouldReturnUsername() {
        // When & Then
        assertThat(userPrincipal.getUsername()).isEqualTo("testuser");
    }

    @Test
    void accountFlags_ShouldReturnTrue() {
        // When & Then
        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
        assertThat(userPrincipal.isEnabled()).isTrue();
    }

    @Test
    void getAuthorities_ShouldHandleDifferentRoles() {
        // Given
        testUser.setRole("USER");
        UserPrincipal userUserPrincipal = new UserPrincipal(testUser);

        // When
        Collection<? extends GrantedAuthority> authorities = userUserPrincipal.getAuthorities();

        // Then
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }
}
