package com.rkscientificindustries.invoice.backend.invoice;

import com.rkscientificindustries.invoice.backend.utils.State;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Table("invoices")
@Data
@Builder
public class Invoice {
  @Id
  private Long id;

  private String invoiceNumber;

  private LocalDate invoiceDate;

  private Long billedTo;

  private Long shippedTo;

  private State place;

  private Transport transport;

  private String courierName;

  private String vehicleNumber;

  private String eWayBillNumber;

  @Min(value = 1, message = "Number of packages must be at least 1")
  private Integer packageCount;

  @NotEmpty(message = "Invoice must have at least one item")
  @MappedCollection(idColumn = "invoice_id", keyColumn = "line_order")
  private List<LineItem> items;

  @DecimalMin(value = "0.0")
  private BigDecimal subtotal;

  @DecimalMin(value = "0.0")
  private BigDecimal discountPercentage;

  @DecimalMin(value = "0.0")
  private BigDecimal totalTax;

  @DecimalMin(value = "0.0")
  private BigDecimal totalAmount;

  @CreatedDate
  private Instant createdDate;

  @LastModifiedDate
  private final Instant lastModifiedDate;

  @Version
  int version;

  public void addLineItem(LineItem item) {
    this.items.add(item);
  }

  public void removeLineItem(LineItem item) {
    this.items.remove(item);
  }

  public enum Transport {
    SELF, COURIER
  }

  @Table("line_items")
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class LineItem {
      @Id
      private Long id;

      private Integer lineOrder;

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
}
