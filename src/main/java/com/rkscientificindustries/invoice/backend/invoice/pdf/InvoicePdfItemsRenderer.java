package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.format;
import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.formatRate;
import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.safe;

@Component
class InvoicePdfItemsRenderer {
  private final PdfCellFactory cellFactory;
  private final InvoicePdfTotalsCalculator totalsCalculator;

  InvoicePdfItemsRenderer(PdfCellFactory cellFactory, InvoicePdfTotalsCalculator totalsCalculator) {
    this.cellFactory = cellFactory;
    this.totalsCalculator = totalsCalculator;
  }

  InvoicePdfTotals render(Document document,
                          Invoice invoice,
                          Map<Long, Product> productMap,
                          InvoicePdfFonts fonts) {
    var table = new PdfPTable(8);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{7, 36, 8, 8, 9, 12, 8, 12});

    addHeaderCell(table, "Sr No.", Element.ALIGN_CENTER, fonts);
    addHeaderCell(table, "Description of Goods", Element.ALIGN_LEFT, fonts);
    addHeaderCell(table, "HSN Code", Element.ALIGN_CENTER, fonts);
    addHeaderCell(table, "Qty", Element.ALIGN_CENTER, fonts);
    addHeaderCell(table, "Unit", Element.ALIGN_CENTER, fonts);
    addHeaderCell(table, "Price", Element.ALIGN_RIGHT, fonts);
    addHeaderCell(table, "Gst", Element.ALIGN_RIGHT, fonts);
    addHeaderCell(table, "Amount", Element.ALIGN_RIGHT, fonts);

    int sr = 1;
    for (LineItem item : invoice.getItems()) {
      var product = productMap.get(item.getProductId());
      var qty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0);
      var unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
      var gstRate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
      var amount = totalsCalculator.calculateAmount(item);

      addDataCell(table, String.valueOf(sr++), Element.ALIGN_CENTER, fonts);
      addDataCell(table,
          product != null ? safe(product.getName()) : "Product #" + safe(item.getProductId()),
          Element.ALIGN_LEFT,
          fonts);
      addDataCell(table, product != null ? safe(product.getHsnCode()) : "", Element.ALIGN_CENTER, fonts);
      addDataCell(table, qty.stripTrailingZeros().toPlainString(), Element.ALIGN_CENTER, fonts);
      addDataCell(table,
          product != null && product.getUnit() != null ? product.getUnit().name() : "Nos",
          Element.ALIGN_CENTER,
          fonts);
      addDataCell(table, format(unitPrice), Element.ALIGN_RIGHT, fonts);
      addDataCell(table, formatRate(gstRate) + "%", Element.ALIGN_RIGHT, fonts);
      addDataCell(table, format(amount), Element.ALIGN_RIGHT, fonts);
    }

    table.setSpacingAfter(0f);
    document.add(table);

    return totalsCalculator.calculate(invoice);
  }

  private void addHeaderCell(PdfPTable table, String text, int alignment, InvoicePdfFonts fonts) {
    var cell = cellFactory.boxedCell(text, fonts.header(), alignment, 4f);
    table.addCell(cell);
  }

  private void addDataCell(PdfPTable table, String text, int alignment, InvoicePdfFonts fonts) {
    var cell = cellFactory.boxedCell(text, fonts.body(), alignment, 3.5f);
    cell.setBorder(Rectangle.BOX);
    table.addCell(cell);
  }
}
