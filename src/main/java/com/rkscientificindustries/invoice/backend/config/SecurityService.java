package com.rkscientificindustries.invoice.backend.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SecurityService {
  public String getAuthenticatedUsername() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()
        && !Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
      return authentication.getName();
    }
    return "Unknown";
  }

  public boolean isAuthenticated() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated()
        && !Objects.equals(authentication.getPrincipal(), "anonymousUser");
  }

  public String getLogoutUrl() {
    return "/logout";
  }
}
