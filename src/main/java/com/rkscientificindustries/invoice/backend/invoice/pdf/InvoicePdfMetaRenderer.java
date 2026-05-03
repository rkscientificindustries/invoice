package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.formatState;
import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.safe;

@Component
class InvoicePdfMetaRenderer {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

  private final PdfCellFactory cellFactory;

  InvoicePdfMetaRenderer(PdfCellFactory cellFactory) {
    this.cellFactory = cellFactory;
  }

  void render(Document document, Invoice invoice, InvoicePdfFonts fonts) {
    var table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{52, 48});

    var left = new PdfPCell();
    left.setPadding(0);
    left.setBorder(Rectangle.BOX);
    left.addElement(buildLabeledLinesTable(List.of(
        new String[]{"Invoice No.", safe(invoice.getInvoiceNumber())},
        new String[]{"Dated", invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FMT) : ""},
        new String[]{"Place of Supply", formatState(invoice.getPlace() != null ? invoice.getPlace().name() : "")},
        new String[]{"Transport", invoice.getTransport() != null ? invoice.getTransport().displayName() : ""}
    ), fonts));

    var right = new PdfPCell();
    right.setPadding(0);
    right.setBorder(Rectangle.BOX);
    right.addElement(buildLabeledLinesTable(List.of(
        new String[]{"Vehicle No.", safe(invoice.getVehicleNumber())},
        new String[]{"E-Way Bill No.", safe(invoice.getEWayBillNumber())},
        new String[]{"NO. OF BOX", invoice.getPackageCount() != null ? invoice.getPackageCount().toString() : ""}
    ), fonts));

    table.addCell(left);
    table.addCell(right);
    table.setSpacingAfter(0f);

    document.add(table);
  }

  private PdfPTable buildLabeledLinesTable(List<String[]> rows, InvoicePdfFonts fonts) {
    var nested = new PdfPTable(3);
    nested.setWidthPercentage(100);
    nested.setWidths(new float[]{42, 3, 55});

    for (String[] row : rows) {
      var keyCell = cellFactory.borderlessCell(row[0], fonts.body(), Element.ALIGN_LEFT, 2.5f);
      var sepCell = cellFactory.borderlessCell(":", fonts.body(), Element.ALIGN_CENTER, 2.5f);
      var valCell = cellFactory.borderlessCell(safe(row[1]), fonts.body(), Element.ALIGN_LEFT, 2.5f);

      nested.addCell(keyCell);
      nested.addCell(sepCell);
      nested.addCell(valCell);
    }
    return nested;
  }
}
