package com.rkscientificindustries.invoice.backend.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Table("invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
  @Id
  private Long id;

  @NotBlank(message = "Item name cannot be empty")
  @Size(max = 50, message = "Item name must not exceed 50 characters")
  private String name;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  private String description;

  @NotBlank
  @Pattern(regexp = "\\d{4}|\\d{6}", message = "HSN code must be either 4 or 6 digits")
  private String hsnCode;

  @Positive(message = "Quantity must be positive")
  private BigDecimal quantity = BigDecimal.ONE;

  @NotNull
  private Unit unit;

  @DecimalMin(value = "0.0")
  private BigDecimal unitPrice;

  @DecimalMin(value = "0.0")
  private BigDecimal costPrice;

  @DecimalMin(value = "0.0")
  private BigDecimal lineTotal;

  @NotNull
  private ItemType type;

  @NotNull
  private BigDecimal gst;

  private String vendorName;

  @CreatedDate
  Instant createdDate;

  @LastModifiedDate
  Instant lastModifiedDate;

  @Version
  int version;

  public static InvoiceItem of(String name, String description, String hsnCode, Unit unit,
                               BigDecimal unitPrice, BigDecimal costPrice, ItemType type, BigDecimal gstRate, String vendorName) {
    return new InvoiceItem(null, name, description, hsnCode, BigDecimal.ONE, unit, unitPrice, costPrice,
            BigDecimal.ZERO, type, gstRate, vendorName, null, null, 0);
  }

  public BigDecimal getTaxAmount() {
    return lineTotal
            .multiply(gst)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  public BigDecimal getTotalWithTax() {
    return lineTotal.add(getTaxAmount());
  }

  public enum Unit {PAIR, PCS, GM, KG, ML, L}

  public enum ItemType {BO, RM, MA}
}
