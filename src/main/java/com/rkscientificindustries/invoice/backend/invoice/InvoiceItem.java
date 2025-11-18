package com.rkscientificindustries.invoice.backend.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Table("invoice_items")
@Data
@NoArgsConstructor
public class InvoiceItem {
  @Id
  private Long id;

  @NotBlank(message = "Item code cannot be empty")
  @Size(max = 50, message = "Item code must not exceed 50 characters")
  private String name;

  @Size(max = 200, message = "Description must not exceed 200 characters")
  private String description;

  @NotBlank
  @Pattern(regexp = "\\d{4}|\\d{6}", message = "HSN code must be either 4 or 6 digits")
  private String hsnCode;

  @Positive(message = "Quantity must be positive")
  private BigDecimal quantity = BigDecimal.ONE;

  @NotNull
  private Unit unit;

  @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
  private BigDecimal unitPrice = BigDecimal.ZERO;

  @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
  private BigDecimal costPrice = BigDecimal.ZERO;

  @DecimalMin(value = "0.0", message = "Line total cannot be negative")
  private BigDecimal lineTotal = BigDecimal.ZERO;

  @NotNull
  private ItemType type;

  @NotNull
  private BigDecimal gst;

  private String vendorName;

  public InvoiceItem(String name, String description, String hsnCode, Unit unit, BigDecimal unitPrice,
                     BigDecimal costPrice, ItemType type, BigDecimal gst, String vendorName) {
    this.name = name;
    this.description = description;
    this.hsnCode = hsnCode;
    this.unit = unit;
    this.unitPrice = unitPrice;
    this.costPrice = costPrice;
    this.type = type;
    this.gst = gst;
    this.vendorName = vendorName;
  }

  public static InvoiceItem of(String name, String description, String hsnCode, BigDecimal quantity,
                               Unit unit, BigDecimal unitPrice, BigDecimal costPrice, String vendorName,
                               ItemType type, BigDecimal gstRate) {
    InvoiceItem item = new InvoiceItem();
    item.setName(name);
    item.setDescription(description);
    item.setHsnCode(hsnCode);
    item.setQuantity(quantity);
    item.setUnit(unit);
    item.setUnitPrice(unitPrice);
    item.setCostPrice(costPrice);
    item.setVendorName(vendorName);
    item.setType(type);
    item.setGst(gstRate);
    item.setLineTotal(quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP));

    return item;
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
