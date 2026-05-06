package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.product.Product;
import org.jspecify.annotations.NonNull;
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
                                         String termsAndConditions,
                                         @NonNull String copyLabel) {
    byte[] pdfBytes = pdfService.generatePdf(invoice, billedCustomer, shippedCustomer, products, termsAndConditions, copyLabel);

    String fileSuffix = invoice.getInvoiceNumber() != null
        ? String.valueOf(invoice.getInvoiceNumber())
        : "draft";
        
    String copySuffix = "";
    if (!copyLabel.isBlank()) {
        copySuffix = "-" + copyLabel.toLowerCase().replace(" copy", "").replace(" ", "-");
    }
    
    String filename = "invoice-" + fileSuffix + copySuffix + ".pdf";

    return new PdfPreviewPayload(filename, pdfBytes);
  }

  public record PdfPreviewPayload(String filename,
                                  byte[] pdfBytes) {
  }
}
