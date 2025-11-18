package com.rkscientificindustries.invoice.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invoice")
@Data
public class InvoiceProperties {
  /**
   * Represents the number of customers associated with the invoice configuration.
   */
  public Integer customers = 10;

  /**
   * Represents the number of invoice items to generate for demo data.
   */
  public Integer items = 10;
}
