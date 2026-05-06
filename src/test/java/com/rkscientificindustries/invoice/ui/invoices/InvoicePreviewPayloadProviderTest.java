package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoicePreviewPayloadProviderTest {

  @Mock
  private InvoicePdfService pdfService;

  @Test
  @DisplayName("Should generate payload with invoice number based filename")
  void shouldCreatePayloadWithInvoiceNumberFilename() {
    byte[] expected = "%PDF-test".getBytes();
    var invoice = Invoice.builder()
        .invoiceNumber(123L)
        .invoiceDate(LocalDate.now())
        .items(List.of())
        .build();

    var billed = new Customer();
    billed.setName("Test Customer");
    var shipped = new Customer();
    shipped.setName("Ship Customer");

    when(pdfService.generatePdf(invoice, billed, shipped, List.of(), "TNC", "Original Copy"))
        .thenReturn(expected);

    var provider = new InvoicePreviewPayloadProvider(pdfService);
    var payload = provider.createPayload(invoice, billed, shipped, List.of(), "TNC", "Original Copy");

    verify(pdfService).generatePdf(invoice, billed, shipped, List.of(), "TNC", "Original Copy");
    assertThat(payload.filename()).isEqualTo("invoice-123-original.pdf");
    assertThat(payload.pdfBytes()).isEqualTo(expected);
  }

  @Test
  @DisplayName("Should fallback to draft filename when invoice number is missing")
  void shouldFallbackToDraftFilename() {
    byte[] expected = "%PDF-draft".getBytes();

    var invoice = Invoice.builder()
        .invoiceNumber(null)
        .invoiceDate(LocalDate.now())
        .items(List.of())
        .build();

    var billed = new Customer();
    billed.setName("Fallback Customer");

    var shipped = new Customer();
    shipped.setName("Ship Customer");

    when(pdfService.generatePdf(invoice, billed, shipped, List.<Product>of(), "TNC", "Original Copy"))
        .thenReturn(expected);

    var provider = new InvoicePreviewPayloadProvider(pdfService);
    var payload = provider.createPayload(invoice, billed, shipped, List.of(), "TNC", "Original Copy");

    verify(pdfService).generatePdf(invoice, billed, shipped, List.of(), "TNC", "Original Copy");
    assertThat(payload.filename()).isEqualTo("invoice-draft-original.pdf");
    assertThat(payload.pdfBytes()).isEqualTo(expected);
  }
}
