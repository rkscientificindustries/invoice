package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.product.Product;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.safe;

@Service
public class InvoicePdfService {
  private static final float FOOTER_RESERVED_HEIGHT = 165f;

  private final InvoicePdfHeaderRenderer headerRenderer;
  private final InvoicePdfMetaRenderer metaRenderer;
  private final InvoicePdfPartiesRenderer partiesRenderer;
  private final InvoicePdfItemsRenderer itemsRenderer;
  private final InvoicePdfTotalsRenderer totalsRenderer;
  private final InvoicePdfFooterRenderer footerRendererFactory;
  private final InvoiceProperties invoiceProperties;

  private final InvoicePdfFonts fonts = new InvoicePdfFonts(
      new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK),
      new Font(Font.HELVETICA, 23, Font.BOLD, Color.BLACK),
      new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK),
      new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK),
      new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK)
  );

  public InvoicePdfService(InvoicePdfHeaderRenderer headerRenderer,
                           InvoicePdfMetaRenderer metaRenderer,
                           InvoicePdfPartiesRenderer partiesRenderer,
                           InvoicePdfItemsRenderer itemsRenderer,
                           InvoicePdfTotalsRenderer totalsRenderer,
                           InvoicePdfFooterRenderer footerRendererFactory,
                           InvoiceProperties invoiceProperties) {
    this.headerRenderer = headerRenderer;
    this.metaRenderer = metaRenderer;
    this.partiesRenderer = partiesRenderer;
    this.itemsRenderer = itemsRenderer;
    this.totalsRenderer = totalsRenderer;
    this.footerRendererFactory = footerRendererFactory;
    this.invoiceProperties = invoiceProperties;
  }

  public byte[] generatePdf(Invoice invoice, Customer billedCustomer, Customer shippedCustomer,
                            List<Product> products, String termsAndConditions) {
    try (var out = new ByteArrayOutputStream()) {
      var document = new Document(PageSize.A4, 18, 18, 18, FOOTER_RESERVED_HEIGHT);
      var writer = PdfWriter.getInstance(document, out);
      writer.setPageEvent(footerRendererFactory.create(invoiceProperties, safe(termsAndConditions), fonts));
      document.open();

      var productMap = buildProductMap(products);

      headerRenderer.render(document, invoiceProperties, fonts);
      metaRenderer.render(document, invoice, fonts);
      partiesRenderer.render(document, billedCustomer, shippedCustomer, fonts);
      InvoicePdfTotals totals = itemsRenderer.render(document, invoice, productMap, fonts);
      totalsRenderer.render(document, totals, fonts);

      document.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new InvoicePdfException("Failed to generate invoice PDF", e);
    }
  }

  private HashMap<Long, Product> buildProductMap(List<Product> products) {
    var map = new HashMap<Long, Product>();
    for (Product p : products) {
      map.put(p.getId(), p);
    }
    return map;
  }

  public static class InvoicePdfException extends RuntimeException {
    public InvoicePdfException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
