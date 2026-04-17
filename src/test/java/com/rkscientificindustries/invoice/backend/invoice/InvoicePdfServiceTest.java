package com.rkscientificindustries.invoice.backend.invoice;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerType;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.Product.ItemType;
import com.rkscientificindustries.invoice.backend.product.Product.Unit;
import com.rkscientificindustries.invoice.backend.utils.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicePdfServiceTest {

  @Test
  @DisplayName("Should generate a valid PDF for invoice preview layout")
  void shouldGenerateValidPdf() {
    var service = new InvoicePdfService(new InvoiceProperties());

    var billedCustomer = new Customer(
        1L,
        "Atomplus Technologies Pvt. Ltd.",
        "billing@atomplus.com",
        "+91 9876543210",
        CustomerType.BUSINESS,
        "29AAXCA5871B1Z8",
        "3rd Floor, SY NO 91/1",
        "Bengaluru",
        State.KARNATAKA,
        "560049",
        null,
        null,
        0
    );

    var shippedCustomer = new Customer(
        2L,
        "Atomplus Technologies Pvt. Ltd.",
        "shipping@atomplus.com",
        "+91 9876500000",
        CustomerType.BUSINESS,
        "21AAMCR5070Q1ZW",
        "Jharagadia, Village Dhenkanal",
        "Dhenkanal",
        State.ODISHA,
        "759025",
        null,
        null,
        0
    );

    var product = new Product(
        10L,
        "Beaker 500 ml",
        "Laboratory beaker",
        "7017",
        Unit.PCS,
        new BigDecimal("73.00"),
        new BigDecimal("70.00"),
        ItemType.BO,
        new BigDecimal("18.00"),
        "Vendor A",
        null,
        null,
        0
    );

    var item = LineItem.builder()
        .id(100L)
        .productId(product.getId())
        .quantity(12)
        .unitPrice(new BigDecimal("73.00"))
        .gstRate(new BigDecimal("18.00"))
        .taxAmount(new BigDecimal("157.68"))
        .totalAmount(new BigDecimal("1033.68"))
        .build();

    var invoice = Invoice.builder()
        .id(77L)
        .invoiceNumber("106")
        .invoiceDate(LocalDate.of(2025, 12, 22))
        .billedTo(1L)
        .shippedTo(2L)
        .place(State.ODISHA)
        .transport(Invoice.Transport.COURIER)
        .vehicleNumber("OD02AB1234")
        .eWayBillNumber("1234567890")
        .packageCount(20)
        .items(List.of(item))
        .subtotal(new BigDecimal("876.00"))
        .discountPercentage(BigDecimal.ZERO)
        .totalTax(new BigDecimal("157.68"))
        .totalAmount(new BigDecimal("1033.68"))
        .status(InvoiceStatus.FINALIZED)
        .build();

    byte[] pdf = service.generatePdf(invoice, billedCustomer, shippedCustomer, List.of(product), "E& O.E");

    assertThat(pdf).isNotNull();
    assertThat(pdf.length).isGreaterThan(1_500);
    assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
  }

  @Test
  @DisplayName("Should fallback to billed customer when shipped customer is null")
  void shouldFallbackToBilledCustomerWhenShippedCustomerMissing() {
    var service = new InvoicePdfService(new InvoiceProperties());

    var customer = new Customer(
        1L,
        "Demo Customer",
        "demo@customer.com",
        "+91 9999999999",
        CustomerType.BUSINESS,
        "29ABCDE1234F1Z5",
        "Street 1",
        "Pune",
        State.MAHARASHTRA,
        "411001",
        null,
        null,
        0
    );

    var product = new Product(
        11L,
        "Pipette 10 ml",
        "Pipette",
        "7017",
        Unit.PCS,
        new BigDecimal("100.00"),
        new BigDecimal("90.00"),
        ItemType.BO,
        new BigDecimal("18.00"),
        "Vendor B",
        null,
        null,
        0
    );

    var invoice = Invoice.builder()
        .invoiceNumber("INV-10")
        .invoiceDate(LocalDate.now())
        .billedTo(1L)
        .place(State.MAHARASHTRA)
        .transport(Invoice.Transport.SELF)
        .packageCount(1)
        .items(List.of(LineItem.builder()
            .productId(11L)
            .quantity(1)
            .unitPrice(new BigDecimal("100.00"))
            .gstRate(new BigDecimal("18.00"))
            .build()))
        .subtotal(new BigDecimal("100.00"))
        .totalTax(new BigDecimal("18.00"))
        .totalAmount(new BigDecimal("118.00"))
        .build();

    byte[] pdf = service.generatePdf(invoice, customer, null, List.of(product), "Standard terms");

    assertThat(pdf).isNotNull();
    assertThat(pdf.length).isGreaterThan(1_000);
    assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
  }
}
