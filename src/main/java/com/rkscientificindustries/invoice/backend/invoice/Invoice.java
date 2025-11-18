package com.rkscientificindustries.invoice.backend.invoice;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.utils.State;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Table("invoices")
@Data
public class Invoice {
  private String invoiceNumber;

  @PastOrPresent(message = "Invoice date cannot be in the future")
  private LocalDate invoiceDate;

  @Valid
  private Customer billedTo;

  @Valid
  private Customer shippedTo;

  private State place;

  private Transport transport;

  // Required if this.transport = COURIER
  private String courierName;

  private String vehicleNumber;

  // Required if this.totalAmount > 50k
  private String eWayBillNumber;

  @Min(value = 1, message = "Number of packages must be at least 1")
  private Integer numberOfPackages;

  @NotEmpty(message = "Invoice must have at least one item")
  @Valid
  private List<InvoiceItem> items;

  @DecimalMin(value = "0.0")
  private BigDecimal subtotal;

  @DecimalMin(value = "0.0")
  private BigDecimal discountPercentage;

  @DecimalMin(value = "0.0")
  private BigDecimal totalTax;

  @DecimalMin(value = "0.0")
  private BigDecimal totalAmount;

  public enum Transport {SELF, COURIER}
}
