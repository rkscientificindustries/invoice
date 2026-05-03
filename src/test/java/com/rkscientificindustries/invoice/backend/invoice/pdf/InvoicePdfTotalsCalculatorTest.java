package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicePdfTotalsCalculatorTest {

  private final InvoicePdfTotalsCalculator calculator = new InvoicePdfTotalsCalculator();

  @Test
  @DisplayName("Should calculate taxable amount, tax and round-off from invoice lines")
  void shouldCalculateTotalsFromInvoiceLines() {
    var invoice = Invoice.builder()
        .items(List.of(
            LineItem.builder().quantity(2).unitPrice(new BigDecimal("100.00")).gstRate(new BigDecimal("18.00")).build(),
            LineItem.builder().quantity(1).unitPrice(new BigDecimal("50.00")).gstRate(BigDecimal.ZERO).build()
        ))
        .totalAmount(new BigDecimal("286.00"))
        .build();

    var totals = calculator.calculate(invoice);

    assertThat(totals.taxableTotal()).isEqualByComparingTo("250.00");
    assertThat(totals.taxTotal()).isEqualByComparingTo("36.00");
    assertThat(totals.grandTotal()).isEqualByComparingTo("286.00");
    assertThat(totals.roundOff()).isEqualByComparingTo("0.00");
    assertThat(totals.taxByRate()).containsEntry(new BigDecimal("18.00"), new BigDecimal("36.00"));
    assertThat(totals.taxByRate()).containsEntry(BigDecimal.ZERO, new BigDecimal("0.00"));
  }
}


