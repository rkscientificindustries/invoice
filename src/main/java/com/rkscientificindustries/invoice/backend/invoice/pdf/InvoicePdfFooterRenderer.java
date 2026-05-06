package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

@Component
class InvoicePdfFooterRenderer {

  InvoicePdfFooterRenderer() { }

  PdfPageEventHelper create(InvoiceProperties invoiceProperties,
                            String termsAndConditions,
                            InvoicePdfFonts fonts) {
    return new FooterRenderer(invoiceProperties, termsAndConditions, fonts);
  }

  private static final class FooterRenderer extends PdfPageEventHelper {
    private final InvoiceProperties invoiceProperties;
    private final String termsAndConditions;
    private final InvoicePdfFonts fonts;

    private FooterRenderer(InvoiceProperties invoiceProperties,
                           String termsAndConditions,
                           InvoicePdfFonts fonts) {
      this.invoiceProperties = invoiceProperties;
      this.termsAndConditions = termsAndConditions;
      this.fonts = fonts;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      PdfContentByte canvas = writer.getDirectContent();
      float left = document.left();
      float width = document.right() - document.left();

      var footerTable = createFooterTable(width);
      float footerBottomY = document.getPageSize().getBottom() + 18f;
      float footerTopY = footerBottomY + footerTable.getTotalHeight();
      footerTable.writeSelectedRows(0, -1, left, footerTopY, canvas);
    }

    private PdfPTable createFooterTable(float width) {
      var footerTable = new PdfPTable(2);
      footerTable.setTotalWidth(width);
      footerTable.setWidths(new float[]{52, 48});

      var bankCell = createBankInfoCell();
      bankCell.setColspan(2);
      footerTable.addCell(bankCell);

      var termsCell = new PdfPCell();
      termsCell.setBorder(Rectangle.BOX);
      termsCell.setPaddingLeft(2f);
      termsCell.setPaddingTop(0f);
      termsCell.addElement(new Paragraph("Terms & Condition", fonts.bodyBold()));
      termsCell.addElement(new Paragraph(termsAndConditions, fonts.body()));

      footerTable.addCell(termsCell);
      footerTable.addCell(createSignatureCell());
      return footerTable;
    }

    private PdfPCell createSignatureCell() {
      var signCell = new PdfPCell();
      signCell.setBorder(Rectangle.BOX);
      signCell.setPadding(0f);

      var signLayout = new PdfPTable(1);
      signLayout.setWidthPercentage(100f);

      signLayout.addCell(createReceiverSection());
      signLayout.addCell(createCompanySection());

      signCell.addElement(signLayout);
      return signCell;
    }

    private PdfPCell createReceiverSection() {
      var receiverCell = new PdfPCell();
      receiverCell.setBorder(Rectangle.BOTTOM);
      receiverCell.setPaddingLeft(2f);
      receiverCell.setPaddingTop(0f);

      var receiverLayout = new PdfPTable(1);
      receiverLayout.setWidthPercentage(100f);

      var labelCell = new PdfPCell();
      labelCell.setBorder(Rectangle.NO_BORDER);
      labelCell.setPadding(0f);
      labelCell.addElement(new Paragraph("Receiver's Signature :", fonts.bodyBold()));
      receiverLayout.addCell(labelCell);

      var spacerCell = new PdfPCell();
      spacerCell.setBorder(Rectangle.NO_BORDER);
      spacerCell.setPadding(0f);
      spacerCell.setMinimumHeight(42f);
      receiverLayout.addCell(spacerCell);

      receiverCell.addElement(receiverLayout);
      return receiverCell;
    }

    private PdfPCell createCompanySection() {
      var companyCell = new PdfPCell();
      companyCell.setBorder(Rectangle.NO_BORDER);
      companyCell.setPadding(4f);

      var companyLayout = new PdfPTable(1);
      companyLayout.setWidthPercentage(100f);

      var companyLabelCell = new PdfPCell();
      companyLabelCell.setBorder(Rectangle.NO_BORDER);
      companyLabelCell.setPadding(0f);
      var companySignatory = new Paragraph("For " + invoiceProperties.getPdf().getCompany().getName(),
          fonts.bodyBold());
      companySignatory.setAlignment(Element.ALIGN_RIGHT);
      companyLabelCell.addElement(companySignatory);
      companyLayout.addCell(companyLabelCell);

      var companySpacerCell = new PdfPCell();
      companySpacerCell.setBorder(Rectangle.NO_BORDER);
      companySpacerCell.setPadding(0f);
      companySpacerCell.setMinimumHeight(32f);
      companyLayout.addCell(companySpacerCell);

      var signatoryLabelCell = new PdfPCell();
      signatoryLabelCell.setBorder(Rectangle.NO_BORDER);
      signatoryLabelCell.setPadding(0f);
      var signatoryLabel = new Paragraph("Authorised Signatory", fonts.bodyBold());
      signatoryLabel.setAlignment(Element.ALIGN_RIGHT);
      signatoryLabelCell.addElement(signatoryLabel);
      companyLayout.addCell(signatoryLabelCell);

      companyCell.addElement(companyLayout);
      return companyCell;
    }

    private PdfPCell createBankInfoCell() {
      var bank = invoiceProperties.getPdf().getBank();
      var bankLine = new Phrase();
      bankLine.add(new Chunk("BANK DETAILS : ", fonts.bodyBold()));
      bankLine.add(new Chunk(bank.getBankName(), fonts.body()));
      bankLine.add(new Chunk("  A/C NO. : ", fonts.bodyBold()));
      bankLine.add(new Chunk(bank.getAccountNumber(), fonts.body()));
      bankLine.add(new Chunk("  IFSC CODE : ", fonts.bodyBold()));
      bankLine.add(new Chunk(bank.getIfsc(), fonts.body()));

      var bankCell = new PdfPCell(bankLine);
      bankCell.setHorizontalAlignment(Element.ALIGN_LEFT);
      bankCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
      bankCell.setBorder(Rectangle.BOX);
      bankCell.setPadding(3f);
      return bankCell;
    }
  }
}
