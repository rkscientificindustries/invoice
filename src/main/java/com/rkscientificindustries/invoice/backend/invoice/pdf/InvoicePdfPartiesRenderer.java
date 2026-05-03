package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.formatState;
import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.safe;

@Component
class InvoicePdfPartiesRenderer {

  void render(Document document,
              Customer billedCustomer,
              Customer shippedCustomer,
              InvoicePdfFonts fonts) {
    var table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{52, 48});

    table.addCell(partyCell("Billed to :", billedCustomer, fonts));
    table.addCell(partyCell("Shipped to :", shippedCustomer, fonts));
    table.setSpacingAfter(0f);

    document.add(table);
  }

  private PdfPCell partyCell(String title, Customer customer, InvoicePdfFonts fonts) {
    var cell = new PdfPCell();
    cell.setBorder(Rectangle.BOX);
    cell.setPadding(2f);
    cell.setPaddingTop(0f);

    var heading = new Paragraph(title, fonts.bodyBold());
    heading.setSpacingAfter(3f);
    cell.addElement(heading);

    if (customer == null) {
      cell.addElement(new Paragraph("-", fonts.body()));
      return cell;
    }

    cell.addElement(compactParagraph(safe(customer.getName()).toUpperCase(Locale.ENGLISH), fonts.bodyBold()));
    cell.addElement(compactParagraph(safe(customer.getStreet()), fonts.body()));
    cell.addElement(compactParagraph(
        safe(customer.getCity()) + ", " + formatState(customer.getState() != null ? customer.getState().name() : ""),
        fonts.body()));
    cell.addElement(compactParagraph("Pin Code:- " + safe(customer.getPostalCode()), fonts.body()));
    cell.addElement(compactParagraph("Party Mobile No. : " + safe(customer.getPhone()), fonts.body()));
    cell.addElement(compactParagraph("GSTIN / UIN : " + safe(customer.getGstin()), fonts.body()));
    return cell;
  }

  private Paragraph compactParagraph(String text, org.openpdf.text.Font font) {
    var p = new Paragraph(safe(text), font);
    p.setLeading(10f);
    p.setSpacingAfter(0f);
    return p;
  }
}
