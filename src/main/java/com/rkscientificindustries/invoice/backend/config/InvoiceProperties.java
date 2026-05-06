package com.rkscientificindustries.invoice.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invoice")
@Data
public class InvoiceProperties {
  /// Represents the number of customers associated with the invoice configuration.
  private Integer customers;

  /// Represents the number of products to generate for demo data.
  private Integer products;

  /// Represents the number of invoices to generate for demo data.
  private Integer invoices;

  /// Configurable PDF rendering values.
  private PdfProperties pdf = new PdfProperties();

  @Data
  public static class PdfProperties {
    private CompanyProperties company = new CompanyProperties();
    private BankProperties bank = new BankProperties();
  }

  @Data
  public static class CompanyProperties {
    private String name;
    private String gstin;
    private String address;
    private String phone;
    private String email;
    private String logoPath;
  }

  @Data
  public static class BankProperties {
    private String bankName;
    private String accountNumber;
    private String ifsc;
  }
}
