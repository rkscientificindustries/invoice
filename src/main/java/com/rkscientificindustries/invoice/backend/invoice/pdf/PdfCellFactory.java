package com.rkscientificindustries.invoice.backend.invoice.pdf;

import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.springframework.stereotype.Component;

import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.safe;

@Component
class PdfCellFactory {

  PdfPCell boxedCell(String text, Font font, int alignment, float padding) {
    return create(text, font, alignment, Rectangle.BOX, padding);
  }

  PdfPCell borderlessCell(String text, Font font, int alignment, float padding) {
    return create(text, font, alignment, Rectangle.NO_BORDER, padding);
  }

  PdfPCell create(String text,
                  Font font,
                  int horizontalAlignment,
                  int border,
                  float padding) {
    var cell = new PdfPCell(new Phrase(safe(text), font));
    cell.setHorizontalAlignment(horizontalAlignment);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setBorder(border);
    cell.setPadding(padding);
    return cell;
  }
}
