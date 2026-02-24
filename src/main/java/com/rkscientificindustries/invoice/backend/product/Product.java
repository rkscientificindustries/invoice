package com.rkscientificindustries.invoice.backend.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id
  private Long id;

  @NotBlank(message = "Product name cannot be empty")
  @Size(max = 50, message = "Product name must not exceed 50 characters")
  private String name;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  private String description;

  @NotBlank
  @Pattern(regexp = "\\d{4}|\\d{6}", message = "HSN code must be either 4 or 6 digits")
  private String hsnCode;

  @NotNull
  private Unit unit;

  @DecimalMin(value = "0.0")
  private BigDecimal unitPrice;

  @DecimalMin(value = "0.0")
  private BigDecimal costPrice;

  @NotNull
  private ItemType type;

  @NotNull
  private BigDecimal gstRate;

  private String vendorName;

  @CreatedDate
  Instant createdDate;

  @LastModifiedDate
  Instant lastModifiedDate;

  @Version
  int version;

  public static Product of(String name, String description, String hsnCode, Unit unit, BigDecimal unitPrice,
                           BigDecimal costPrice, ItemType type, BigDecimal gstRate, String vendorName) {
    return new Product(null, name, description, hsnCode, unit, unitPrice, costPrice,
            type, gstRate, vendorName, null, null, 0);
  }

  public enum Unit { PAIR, PCS, GM, KG, ML, L }

  public enum ItemType { BO, RM, MA }
}
