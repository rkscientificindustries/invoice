package com.rkscientificindustries.invoice.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invoice")
@Data
public class InvoiceProperties {
  /// Represents the number of customers associated with the invoice configuration.
  private Integer customers = 10;

  /// Represents the number of products to generate for demo data.
  private Integer products = 10;

  /// Represents the number of invoices to generate for demo data.
  private Integer invoices = 5;

  /// Configurable PDF rendering values.
  private PdfProperties pdf = new PdfProperties();

  @Data
  public static class PdfProperties {
    private String title = "TAX INVOICE";
    private String copyLabel = "Original Copy";
    private CompanyProperties company = new CompanyProperties();
    private BankProperties bank = new BankProperties();
  }

  @Data
  public static class CompanyProperties {
    private String name = "R.K. SCIENTIFIC INDUSTRIES";
    private String gstin = "06CGLPP3030J1ZC";
    private String address = "21A, BABYAL ROAD MAHESH NAGAR, AMBALA CANTT, Ambala, Haryana, 133001";
    private String phone = "+917015539187, +918950959177";
    private String email = "rkscientific.sales@gmail.com";
    private String logoPath = "static/logo-rk-sm.jpg";
    private String signatoryLabel = "For R.K. SCIENTIFIC INDUSTRIES";
  }

  @Data
  public static class BankProperties {
    private String heading = "BANK DETAILS";
    private String bankName = "HDFC BANK, AMBALA CANTT";
    private String accountNumber = "50200049591048";
    private String ifsc = "HDFC0002562";
  }
}
