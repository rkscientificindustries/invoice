package com.rkscientificindustries.invoice.backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityService Tests")
class SecurityConfigTest {
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @Test
  @DisplayName("Password encoder should encode and verify passwords correctly")
  void testPasswordEncoderWorks() {
    String rawPassword = "testPassword123";
    String encodedPassword = passwordEncoder.encode(rawPassword);

    assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
  }

  @Test
  @DisplayName("Should correctly hash different passwords")
  void testPasswordEncoderHashesDifferently() {
    String password = "myPassword";
    String hash1 = passwordEncoder.encode(password);
    String hash2 = passwordEncoder.encode(password);

    // Even with same password, hashes should be different (due to salt)
    assertThat(hash1).isNotEqualTo(hash2);
    // But both should match the original password
    assertThat(passwordEncoder.matches(password, hash1)).isTrue();
    assertThat(passwordEncoder.matches(password, hash2)).isTrue();
  }

  @Test
  @DisplayName("SecurityService should return authenticated username")
  void testSecurityServiceGetAuthenticatedUsername() {
    SecurityService securityService = new SecurityService();

    when(authentication.getName()).thenReturn("admin");
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn("admin");
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    String username = securityService.getAuthenticatedUsername();
    assertThat(username).isEqualTo("admin");

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("SecurityService should return Unknown for unauthenticated user")
  void testSecurityServiceUnknownForUnauthenticatedUser() {
    SecurityService securityService = new SecurityService();
    SecurityContextHolder.clearContext();

    String username = securityService.getAuthenticatedUsername();
    assertThat(username).isEqualTo("Unknown");
  }

  @Test
  @DisplayName("SecurityService should correctly identify authenticated user")
  void testSecurityServiceIsAuthenticated() {
    SecurityService securityService = new SecurityService();

    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn("admin");
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    assertThat(securityService.isAuthenticated()).isTrue();

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("SecurityService should return logout URL")
  void testSecurityServiceGetLogoutUrl() {
    SecurityService securityService = new SecurityService();
    assertThat(securityService.getLogoutUrl()).isEqualTo("/logout");
  }
}
