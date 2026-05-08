package com.rkscientificindustries.invoice.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "admin")
@Data
public class AdminProperties {
  private String username;
  private String password;
}
