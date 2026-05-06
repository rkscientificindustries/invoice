package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;

import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.safe;

@Component
class InvoicePdfHeaderRenderer {
  private final PdfCellFactory cellFactory;

  InvoicePdfHeaderRenderer(PdfCellFactory cellFactory) {
    this.cellFactory = cellFactory;
  }

  void render(Document document, InvoiceProperties invoiceProperties, String copyLabel, InvoicePdfFonts fonts) {
    addTopBar(document, invoiceProperties, copyLabel, fonts);
    addCompanyHeader(document, invoiceProperties, fonts);
  }

  private void addTopBar(Document document, InvoiceProperties invoiceProperties, String copyLabel, InvoicePdfFonts fonts) {
    var pdf = invoiceProperties.getPdf();
    var company = pdf.getCompany();

    var table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{65, 35});

    var gstin = cellFactory.boxedCell("GSTIN: " + safe(company.getGstin()), fonts.bodyBold(), Element.ALIGN_LEFT, 4f);
    var copy = cellFactory.boxedCell(safe(copyLabel), fonts.body(), Element.ALIGN_RIGHT, 4f);
    gstin.setBorder(Rectangle.LEFT | Rectangle.TOP);
    copy.setBorder(Rectangle.RIGHT | Rectangle.TOP);
    gstin.setPaddingTop(2f);
    gstin.setPaddingBottom(2f);
    copy.setPaddingTop(2f);
    copy.setPaddingBottom(2f);

    table.addCell(gstin);
    table.addCell(copy);
    table.setSpacingAfter(0f);

    document.add(table);
  }

  private void addCompanyHeader(Document document, InvoiceProperties invoiceProperties, InvoicePdfFonts fonts) {
    var pdf = invoiceProperties.getPdf();
    var company = pdf.getCompany();

    var table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{16, 84});

    var logoCell = new PdfPCell();
    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    logoCell.setBorder(Rectangle.LEFT | Rectangle.BOTTOM);
    logoCell.setPaddingLeft(4f);

    loadLogo(company.getLogoPath()).ifPresentOrElse(image -> {
      try {
        image.scaleToFit(36f, 36f);
        logoCell.addElement(image);
      } catch (Exception e) {
        logoCell.addElement(new Phrase(" ", fonts.body()));
      }
    }, () -> logoCell.addElement(new Phrase(" ", fonts.body())));

    var detailsCell = getDetailsCell(company, fonts);

    table.addCell(logoCell);
    table.addCell(detailsCell);
    table.setSpacingAfter(0f);

    document.add(table);
  }

  private PdfPCell getDetailsCell(InvoiceProperties.CompanyProperties company, InvoicePdfFonts fonts) {
    var detailsCell = new PdfPCell();
    detailsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    detailsCell.setBorder(Rectangle.RIGHT | Rectangle.BOTTOM);
    detailsCell.setPadding(2f);

    var invoiceTitle = new Paragraph(safe("TAX INVOICE"), fonts.title());
    invoiceTitle.setAlignment(Element.ALIGN_CENTER);
    invoiceTitle.setSpacingAfter(1f);

    var companyName = new Paragraph(safe(company.getName()), fonts.companyName());
    companyName.setAlignment(Element.ALIGN_CENTER);
    companyName.setSpacingAfter(1f);

    var address = new Paragraph(safe(company.getAddress()), fonts.body());
    address.setAlignment(Element.ALIGN_CENTER);

    var contact = new Paragraph("Mob. : " + safe(company.getPhone()) + "    email : " + safe(company.getEmail()),
        fonts.bodyBold());
    contact.setAlignment(Element.ALIGN_CENTER);
    contact.setSpacingBefore(0f);

    detailsCell.addElement(invoiceTitle);
    detailsCell.addElement(companyName);
    detailsCell.addElement(address);
    detailsCell.addElement(contact);
    return detailsCell;
  }

  private Optional<Image> loadLogo(String path) {
    if (path == null || path.isBlank()) {
      return Optional.empty();
    }
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
      if (stream == null) {
        return Optional.empty();
      }
      return Optional.of(Image.getInstance(stream.readAllBytes()));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
