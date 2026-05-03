package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.product.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoicePreviewPayloadProvider {
  private final InvoicePdfService pdfService;

  public InvoicePreviewPayloadProvider(InvoicePdfService pdfService) {
    this.pdfService = pdfService;
  }

  public PdfPreviewPayload createPayload(Invoice invoice,
                                         Customer billedCustomer,
                                         Customer shippedCustomer,
                                         List<Product> products,
                                         String termsAndConditions) {
    byte[] pdfBytes = pdfService.generatePdf(invoice, billedCustomer, shippedCustomer, products, termsAndConditions);

    String fileSuffix = invoice.getInvoiceNumber() != null && !invoice.getInvoiceNumber().isBlank()
        ? invoice.getInvoiceNumber()
        : "draft";
    String filename = "invoice-" + fileSuffix + ".pdf";

    return new PdfPreviewPayload(filename, pdfBytes);
  }

  public record PdfPreviewPayload(String filename,
                                  byte[] pdfBytes) {
  }
}
