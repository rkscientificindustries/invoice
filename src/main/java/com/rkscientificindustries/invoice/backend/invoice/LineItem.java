package com.rkscientificindustries.invoice.backend.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("line_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineItem {
  @Id
  private Long id;

  @NotNull
  private Long productId;

  @Min(value = 1, message = "Quantity must be at least 1")
  private Integer quantity;

  @DecimalMin(value = "0.0")
  private BigDecimal unitPrice;

  @DecimalMin(value = "0.0")
  private BigDecimal gstRate;

  @DecimalMin(value = "0.0")
  private BigDecimal taxAmount;

  @DecimalMin(value = "0.0")
  private BigDecimal totalAmount;
}
