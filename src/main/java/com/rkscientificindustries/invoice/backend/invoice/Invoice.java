package com.rkscientificindustries.invoice.backend.invoice;

import com.rkscientificindustries.invoice.backend.utils.State;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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

  @Builder.Default
  @MappedCollection(idColumn = "invoice_id", keyColumn = "line_order")
  private List<LineItem> items = new ArrayList<>();

  @DecimalMin(value = "0.0")
  private BigDecimal subtotal;

  @DecimalMin(value = "0.0")
  private BigDecimal discountPercentage;

  @DecimalMin(value = "0.0")
  private BigDecimal totalTax;

  @DecimalMin(value = "0.0")
  private BigDecimal totalAmount;

  @Builder.Default
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @CreatedDate
  private Instant createdDate;

  @LastModifiedDate
  private final Instant lastModifiedDate;

  @Version
  int version;

  public enum Transport {
    SELF("Self"),
    COURIER("Courier");

    private final String displayName;

    Transport(String displayName) {
      this.displayName = displayName;
    }

    public String displayName() {
      return displayName;
    }
  }
}
