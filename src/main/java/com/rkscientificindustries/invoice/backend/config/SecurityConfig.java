package com.rkscientificindustries.invoice.backend.config;

import com.rkscientificindustries.invoice.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private final AdminProperties adminProperties;

  public SecurityConfig(AdminProperties adminProperties) {
    this.adminProperties = adminProperties;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class));

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails admin = User.builder()
        .username(adminProperties.getUsername())
        .password(passwordEncoder().encode(adminProperties.getPassword()))
        .roles("ADMIN")
        .build();

    return new InMemoryUserDetailsManager(admin);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
