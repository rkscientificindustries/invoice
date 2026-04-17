package com.rkscientificindustries.invoice.backend.invoice;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.product.Product;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class InvoicePdfService {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
  private static final String CURRENCY = "₹";
  private static final float FOOTER_RESERVED_HEIGHT = 165f;

  private final InvoiceProperties invoiceProperties;

  private final Font fontTitle = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
  private final Font fontCompanyName = new Font(Font.HELVETICA, 23, Font.BOLD, Color.BLACK);
  private final Font fontHeader = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
  private final Font fontBody = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
  private final Font fontBodyBold = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
  private final Font fontSmall = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);

  public InvoicePdfService(InvoiceProperties invoiceProperties) {
    this.invoiceProperties = invoiceProperties;
  }

  public byte[] generatePdf(Invoice invoice, Customer billedCustomer, Customer shippedCustomer,
                            List<Product> products, String termsAndConditions) {
    try (var out = new ByteArrayOutputStream()) {
      var document = new Document(PageSize.A4, 18, 18, 18, FOOTER_RESERVED_HEIGHT);
      var writer = PdfWriter.getInstance(document, out);
      writer.setPageEvent(new FooterRenderer(invoiceProperties, safe(termsAndConditions), fontBody, fontBodyBold));
      document.open();

      var productMap = buildProductMap(products);
      Customer shipping = shippedCustomer != null ? shippedCustomer : billedCustomer;

      addTopBar(document);
      addCompanyHeader(document);
      addMetaGrid(document, invoice);
      addPartiesGrid(document, billedCustomer, shipping);
      var totals = addItemsTable(document, invoice, productMap);
      addCalculationSection(document, totals);
      addTaxSummarySection(document, totals);
      addAmountInWords(document, totals.grandTotal());

      document.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new InvoicePdfException("Failed to generate invoice PDF", e);
    }
  }

  private void addTopBar(Document document) {
    var pdf = invoiceProperties.getPdf();
    var company = pdf.getCompany();

    var table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{65, 35});

    var gstin = bodyCell("GSTIN: " + safe(company.getGstin()), fontBodyBold, Element.ALIGN_LEFT);
    var copy = bodyCell(safe(pdf.getCopyLabel()), fontBody, Element.ALIGN_RIGHT);
    gstin.setBorder(Rectangle.LEFT | Rectangle.TOP);
    copy.setBorder(Rectangle.RIGHT | Rectangle.TOP);
    gstin.setPaddingBottom(6f);
    copy.setPaddingBottom(6f);

    table.addCell(gstin);
    table.addCell(copy);
    setOuterSectionBorders(table);

    document.add(table);
  }

  private void addCompanyHeader(Document document) {
    var pdf = invoiceProperties.getPdf();
    var company = pdf.getCompany();

    var table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{16, 84});

    var logoCell = new PdfPCell();
    logoCell.setPadding(6f);
    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    logoCell.setBorder(Rectangle.LEFT | Rectangle.BOTTOM);

    loadLogo(company.getLogoPath()).ifPresentOrElse(image -> {
      try {
        image.scaleToFit(56f, 56f);
        logoCell.addElement(image);
      } catch (Exception e) {
        logoCell.addElement(new Phrase(" ", fontBody));
      }
    }, () -> logoCell.addElement(new Phrase(" ", fontBody)));

    var detailsCell = new PdfPCell();
    detailsCell.setPadding(4f);
    detailsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
    detailsCell.setBorder(Rectangle.RIGHT | Rectangle.BOTTOM);

    var invoiceTitle = new Paragraph(safe(pdf.getTitle()), fontTitle);
    invoiceTitle.setAlignment(Element.ALIGN_CENTER);
    invoiceTitle.setSpacingAfter(4f);

    var companyName = new Paragraph(safe(company.getName()), fontCompanyName);
    companyName.setAlignment(Element.ALIGN_CENTER);
    companyName.setSpacingAfter(4f);

    var address = new Paragraph(safe(company.getAddress()), fontBody);
    address.setAlignment(Element.ALIGN_CENTER);

    var contact = new Paragraph("Mob. : " + safe(company.getPhone()) + "    email : " + safe(company.getEmail()), fontBodyBold);
    contact.setAlignment(Element.ALIGN_CENTER);
    contact.setSpacingBefore(2f);

    detailsCell.addElement(invoiceTitle);
    detailsCell.addElement(companyName);
    detailsCell.addElement(address);
    detailsCell.addElement(contact);

    table.addCell(logoCell);
    table.addCell(detailsCell);
    setOuterSectionBorders(table);

    document.add(table);
  }

  private void addMetaGrid(Document document, Invoice invoice) {
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
    )));

    var right = new PdfPCell();
    right.setPadding(0);
    right.setBorder(Rectangle.BOX);
    right.addElement(buildLabeledLinesTable(List.of(
        new String[]{"Vehicle No.", safe(invoice.getVehicleNumber())},
        new String[]{"E-Way Bill No.", safe(invoice.getEWayBillNumber())},
        new String[]{"NO. OF BOX", invoice.getPackageCount() != null ? invoice.getPackageCount().toString() : ""}
    )));

    table.addCell(left);
    table.addCell(right);
    setOuterSectionBorders(table);

    document.add(table);
  }

  private PdfPTable buildLabeledLinesTable(List<String[]> rows) {
    var nested = new PdfPTable(3);
    nested.setWidthPercentage(100);
    nested.setWidths(new float[]{42, 3, 55});

    for (String[] row : rows) {
      var keyCell = bodyCell(row[0], fontBody, Element.ALIGN_LEFT);
      var sepCell = bodyCell(":", fontBody, Element.ALIGN_CENTER);
      var valCell = bodyCell(safe(row[1]), fontBody, Element.ALIGN_LEFT);

      keyCell.setBorder(Rectangle.NO_BORDER);
      sepCell.setBorder(Rectangle.NO_BORDER);
      valCell.setBorder(Rectangle.NO_BORDER);
      keyCell.setPadding(2.5f);
      sepCell.setPadding(2.5f);
      valCell.setPadding(2.5f);

      nested.addCell(keyCell);
      nested.addCell(sepCell);
      nested.addCell(valCell);
    }

    return nested;
  }

  private void addPartiesGrid(Document document, Customer billedCustomer, Customer shippedCustomer) {
    var table = new PdfPTable(2);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{52, 48});

    table.addCell(partyCell("Billed to :", billedCustomer));
    table.addCell(partyCell("Shipped to :", shippedCustomer));
    setOuterSectionBorders(table);

    document.add(table);
  }

  private PdfPCell partyCell(String title, Customer customer) {
    var cell = new PdfPCell();
    cell.setBorder(Rectangle.BOX);
    cell.setPadding(4f);

    var heading = new Paragraph(title, fontBodyBold);
    heading.setSpacingAfter(3f);
    cell.addElement(heading);

    if (customer == null) {
      cell.addElement(new Paragraph("-", fontBody));
      return cell;
    }

    cell.addElement(compactParagraph(safe(customer.getName()).toUpperCase(Locale.ENGLISH), fontBodyBold));
    cell.addElement(compactParagraph(safe(customer.getStreet()), fontBody));
    cell.addElement(compactParagraph(safe(customer.getCity()) + ", " + formatState(customer.getState() != null ? customer.getState()
                                                                                                                 .name() : ""), fontBody));
    cell.addElement(compactParagraph("Pin Code:- " + safe(customer.getPostalCode()), fontBody));
    cell.addElement(compactParagraph("Party Mobile No. : " + safe(customer.getPhone()), fontBody));
    cell.addElement(compactParagraph("GSTIN / UIN : " + safe(customer.getGstin()), fontBody));
    return cell;
  }

  private Totals addItemsTable(Document document, Invoice invoice, HashMap<Long, Product> productMap) {
    var table = new PdfPTable(8);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{7, 36, 8, 8, 9, 12, 8, 12});

    addHeaderCell(table, "Sr No.", Element.ALIGN_CENTER);
    addHeaderCell(table, "Description of Goods", Element.ALIGN_LEFT);
    addHeaderCell(table, "HSN Code", Element.ALIGN_CENTER);
    addHeaderCell(table, "Qty", Element.ALIGN_CENTER);
    addHeaderCell(table, "Unit", Element.ALIGN_CENTER);
    addHeaderCell(table, "Price", Element.ALIGN_RIGHT);
    addHeaderCell(table, "Gst", Element.ALIGN_RIGHT);
    addHeaderCell(table, "Amount(" + CURRENCY + ")", Element.ALIGN_RIGHT);

    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    var taxableByRate = new TreeMap<BigDecimal, BigDecimal>();
    var taxByRate = new TreeMap<BigDecimal, BigDecimal>();

    int sr = 1;
    for (LineItem item : invoice.getItems()) {
      var product = productMap.get(item.getProductId());
      var qty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0);
      var unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
      var gstRate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
      var amount = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
      var taxAmount = amount.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

      subtotal = subtotal.add(amount);
      totalTax = totalTax.add(taxAmount);
      taxableByRate.merge(gstRate, amount, BigDecimal::add);
      taxByRate.merge(gstRate, taxAmount, BigDecimal::add);

      addDataCell(table, String.valueOf(sr++), Element.ALIGN_CENTER);
      addDataCell(table, product != null ? safe(product.getName()) : "Product #" + safe(item.getProductId()), Element.ALIGN_LEFT);
      addDataCell(table, product != null ? safe(product.getHsnCode()) : "", Element.ALIGN_CENTER);
      addDataCell(table, qty.stripTrailingZeros().toPlainString(), Element.ALIGN_CENTER);
      addDataCell(table, product != null && product.getUnit() != null ? product.getUnit()
                                                                        .name() : "Nos", Element.ALIGN_CENTER);
      addDataCell(table, format(unitPrice), Element.ALIGN_RIGHT);
      addDataCell(table, formatRate(gstRate) + "%", Element.ALIGN_RIGHT);
      addDataCell(table, format(amount), Element.ALIGN_RIGHT);
    }

    setOuterSectionBorders(table);

    document.add(table);

    BigDecimal computedGross = subtotal.add(totalTax).setScale(2, RoundingMode.HALF_UP);
    BigDecimal grandTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount()
                                                               .setScale(2, RoundingMode.HALF_UP) : computedGross;
    BigDecimal roundOff = grandTotal.subtract(computedGross).setScale(2, RoundingMode.HALF_UP);

    return new Totals(subtotal, totalTax, grandTotal, roundOff, taxableByRate, taxByRate);
  }

  private void addCalculationSection(Document document, Totals totals) {
    var table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{60, 10, 8, 22});

    addCalcRow(table, "Taxable Amount", "", "", format(totals.taxableTotal()));

    if (totals.taxByRate().isEmpty()) {
      addCalcRow(table, "Add : IGST", "@", "0%", format(BigDecimal.ZERO));
    } else {
      boolean first = true;
      for (Map.Entry<BigDecimal, BigDecimal> entry : totals.taxByRate().entrySet()) {
        String label = first ? "Add : IGST" : "";
        addCalcRow(table, label, "@", formatRate(entry.getKey()) + "%", format(entry.getValue()));
        first = false;
      }
    }

    addCalcRow(table, "Transportation", "", "", format(BigDecimal.ZERO));
    addCalcRow(table, "Packing", "", "", format(BigDecimal.ZERO));
    addCalcRow(table, "Roundoff", "", "", format(totals.roundOff()));

    var grandLabel = new PdfPCell(new Phrase("Grand Total", fontBodyBold));
    grandLabel.setColspan(3);
    grandLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
    grandLabel.setPadding(3f);
    grandLabel.setBorder(Rectangle.BOX);

    var grandValue = new PdfPCell(new Phrase(format(totals.grandTotal()), fontBodyBold));
    grandValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
    grandValue.setPadding(3f);
    grandValue.setBorder(Rectangle.BOX);

    table.addCell(grandLabel);
    table.addCell(grandValue);

    setOuterSectionBorders(table);
    document.add(table);
  }

  private void addTaxSummarySection(Document document, Totals totals) {
    var table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{14, 18, 18, 18});

    addSummaryHeaderCell(table, "Tax Rate");
    addSummaryHeaderCell(table, "Taxable Amt.");
    addSummaryHeaderCell(table, "IGST Amt.");
    addSummaryHeaderCell(table, "Total Tax");

    if (totals.taxByRate().isEmpty()) {
      addSummaryDataCell(table, "0%", Element.ALIGN_LEFT);
      addSummaryDataCell(table, format(BigDecimal.ZERO), Element.ALIGN_LEFT);
      addSummaryDataCell(table, format(BigDecimal.ZERO), Element.ALIGN_LEFT);
      addSummaryDataCell(table, format(BigDecimal.ZERO), Element.ALIGN_LEFT);
    } else {
      List<String> rates = new ArrayList<>();
      for (BigDecimal rate : totals.taxByRate().keySet()) {
        rates.add(formatRate(rate) + "%");
      }
      addSummaryDataCell(table, String.join(", ", rates), Element.ALIGN_LEFT);
      addSummaryDataCell(table, format(totals.taxableTotal()), Element.ALIGN_LEFT);
      addSummaryDataCell(table, format(totals.taxTotal()), Element.ALIGN_LEFT);
      addSummaryDataCell(table, format(totals.taxTotal()), Element.ALIGN_LEFT);
    }

    setOuterSectionBorders(table);
    document.add(table);
  }

  private void addAmountInWords(Document document, BigDecimal grandTotal) {
    String words = "Rupees " + numberToWordsIndian(grandTotal.abs().setScale(0, RoundingMode.HALF_UP)
        .longValue()) + " Only";
    var p = new Paragraph(words, fontBodyBold);
    p.setSpacingBefore(2f);
    p.setSpacingAfter(2f);
    document.add(p);
  }

  private void addCalcRow(PdfPTable table, String label, String at, String percent, String value) {
    table.addCell(calcCell(label, Element.ALIGN_CENTER, false));
    table.addCell(calcCell(at, Element.ALIGN_CENTER, false));
    table.addCell(calcCell(percent, Element.ALIGN_CENTER, false));
    table.addCell(calcCell(value, Element.ALIGN_RIGHT, false));
  }

  private PdfPCell calcCell(String text, int alignment, boolean bold) {
    var cell = new PdfPCell(new Phrase(safe(text), bold ? fontBodyBold : fontBody));
    cell.setHorizontalAlignment(alignment);
    cell.setPadding(3f);
    cell.setBorder(Rectangle.NO_BORDER);
    return cell;
  }

  private void addSummaryHeaderCell(PdfPTable table, String text) {
    var cell = new PdfPCell(new Phrase(text, fontBodyBold));
    cell.setPadding(3f);
    cell.setBorder(Rectangle.NO_BORDER);
    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
    table.addCell(cell);
  }

  private void addSummaryDataCell(PdfPTable table, String text, int alignment) {
    var cell = new PdfPCell(new Phrase(safe(text), fontBody));
    cell.setPadding(3f);
    cell.setBorder(Rectangle.NO_BORDER);
    cell.setHorizontalAlignment(alignment);
    table.addCell(cell);
  }

  private Paragraph compactParagraph(String text, Font font) {
    var p = new Paragraph(safe(text), font);
    p.setLeading(10f);
    p.setSpacingAfter(0f);
    return p;
  }

  private void addFooterBankDetails(PdfPTable table) {
    var bank = invoiceProperties.getPdf().getBank();

    String text = bank.getHeading() + " : " + safe(bank.getBankName())
        + "  A/C NO. : " + safe(bank.getAccountNumber())
        + "\nIFSC CODE : " + safe(bank.getIfsc());

    var cell = new PdfPCell(new Phrase(text, fontBodyBold));
    cell.setBorder(Rectangle.BOX);
    cell.setPadding(3f);
    table.addCell(cell);
  }

  private void addFooterTermsAndSignature(PdfPTable table, String termsAndConditions) throws Exception {
    table.setWidths(new float[]{52, 48});

    var termsCell = new PdfPCell();
    termsCell.setBorder(Rectangle.BOX);
    termsCell.setPadding(3f);
    termsCell.addElement(new Paragraph("Terms & Condition", fontBodyBold));
    termsCell.addElement(new Paragraph(safe(termsAndConditions), fontBody));

    var signCell = new PdfPCell();
    signCell.setBorder(Rectangle.BOX);
    signCell.setPadding(4f);
    signCell.addElement(new Paragraph("Receiver's Signature :", fontBodyBold));
    signCell.addElement(new Paragraph(" ", fontBody));
    signCell.addElement(new Paragraph(" ", fontBody));

    var companySignatory = new Paragraph(safe(invoiceProperties.getPdf().getCompany()
        .getSignatoryLabel()), fontBodyBold);
    companySignatory.setAlignment(Element.ALIGN_RIGHT);
    signCell.addElement(companySignatory);

    var signatoryLabel = new Paragraph("Authorised Signatory", fontBodyBold);
    signatoryLabel.setAlignment(Element.ALIGN_RIGHT);
    signCell.addElement(signatoryLabel);

    table.addCell(termsCell);
    table.addCell(signCell);
  }

  private void addHeaderCell(PdfPTable table, String text, int alignment) {
    var cell = new PdfPCell(new Phrase(text, fontHeader));
    cell.setHorizontalAlignment(alignment);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(4f);
    cell.setBorder(Rectangle.BOX);
    table.addCell(cell);
  }

  private void addDataCell(PdfPTable table, String text, int alignment) {
    var cell = new PdfPCell(new Phrase(text, fontBody));
    cell.setHorizontalAlignment(alignment);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(3.5f);
    cell.setBorder(Rectangle.BOX);
    table.addCell(cell);
  }

  private PdfPCell bodyCell(String text, Font font, int alignment) {
    var cell = new PdfPCell(new Phrase(text, font));
    cell.setHorizontalAlignment(alignment);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(4f);
    cell.setBorder(Rectangle.BOX);
    return cell;
  }

  private void setOuterSectionBorders(PdfPTable table) {
    table.setSpacingAfter(0f);
  }

  private String numberToWordsIndian(long number) {
    if (number == 0) {
      return "Zero";
    }
    String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

    return convertIndian(number, units, tens).trim().replaceAll("\\s+", " ");
  }

  private String convertIndian(long number, String[] units, String[] tens) {
    if (number < 20) {
      return units[(int) number];
    }
    if (number < 100) {
      return tens[(int) (number / 10)] + " " + convertIndian(number % 10, units, tens);
    }
    if (number < 1000) {
      return units[(int) (number / 100)] + " Hundred " + convertIndian(number % 100, units, tens);
    }
    if (number < 100000) {
      return convertIndian(number / 1000, units, tens) + " Thousand " + convertIndian(number % 1000, units, tens);
    }
    if (number < 10000000) {
      return convertIndian(number / 100000, units, tens) + " Lakh " + convertIndian(number % 100000, units, tens);
    }
    return convertIndian(number / 10000000, units, tens) + " Crore " + convertIndian(number % 10000000, units, tens);
  }

  private java.util.Optional<Image> loadLogo(String path) {
    if (path == null || path.isBlank()) {
      return java.util.Optional.empty();
    }
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
      if (stream == null) {
        return java.util.Optional.empty();
      }
      return java.util.Optional.of(Image.getInstance(stream.readAllBytes()));
    } catch (Exception e) {
      return java.util.Optional.empty();
    }
  }

  private HashMap<Long, Product> buildProductMap(List<Product> products) {
    var map = new HashMap<Long, Product>();
    for (Product p : products) {
      map.put(p.getId(), p);
    }
    return map;
  }

  private String format(BigDecimal value) {
    return String.format("%,.2f", value != null ? value : BigDecimal.ZERO);
  }

  private String formatRate(BigDecimal rate) {
    if (rate == null) {
      return "0";
    }
    return rate.stripTrailingZeros().toPlainString();
  }

  private String formatState(String state) {
    if (state == null || state.isBlank()) {
      return "";
    }
    return state.replace('_', ' ');
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private record Totals(BigDecimal taxableTotal,
                        BigDecimal taxTotal,
                        BigDecimal grandTotal,
                        BigDecimal roundOff,
                        Map<BigDecimal, BigDecimal> taxableByRate,
                        Map<BigDecimal, BigDecimal> taxByRate) {
  }

  private static class FooterRenderer extends PdfPageEventHelper {
    private final InvoiceProperties invoiceProperties;
    private final String termsAndConditions;
    private final Font fontBody;
    private final Font fontBodyBold;

    private FooterRenderer(InvoiceProperties invoiceProperties,
                           String termsAndConditions,
                           Font fontBody,
                           Font fontBodyBold) {
      this.invoiceProperties = invoiceProperties;
      this.termsAndConditions = termsAndConditions;
      this.fontBody = fontBody;
      this.fontBodyBold = fontBodyBold;
    }

    private static String value(String v) {
      return v == null ? "" : v;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      try {
        PdfContentByte canvas = writer.getDirectContent();
        float left = document.left();
        float width = document.right() - document.left();

        float bankTopY = document.bottom() - 8f;
        float termsTopY = bankTopY - 36f;

        var bankTable = new PdfPTable(1);
        bankTable.setTotalWidth(width);

        var bank = invoiceProperties.getPdf().getBank();
        String bankText = bank.getHeading() + " : " + value(bank.getBankName())
            + "  A/C NO. : " + value(bank.getAccountNumber())
            + "\nIFSC CODE : " + value(bank.getIfsc());

        var bankCell = new PdfPCell(new Phrase(bankText, fontBodyBold));
        bankCell.setBorder(Rectangle.BOX);
        bankCell.setPadding(3f);
        bankTable.addCell(bankCell);
        bankTable.writeSelectedRows(0, -1, left, bankTopY, canvas);

        var tsTable = new PdfPTable(2);
        tsTable.setTotalWidth(width);
        tsTable.setWidths(new float[]{52, 48});

        var termsCell = new PdfPCell();
        termsCell.setBorder(Rectangle.BOX);
        termsCell.setPadding(3f);
        termsCell.addElement(new Paragraph("Terms & Condition", fontBodyBold));
        termsCell.addElement(new Paragraph(value(termsAndConditions), fontBody));

        var signCell = new PdfPCell();
        signCell.setBorder(Rectangle.BOX);
        signCell.setPadding(3f);
        signCell.addElement(new Paragraph("Receiver's Signature :", fontBodyBold));
        signCell.addElement(new Paragraph(" ", fontBody));
        signCell.addElement(new Paragraph(" ", fontBody));

        var companySignatory = new Paragraph(value(invoiceProperties.getPdf().getCompany()
            .getSignatoryLabel()), fontBodyBold);
        companySignatory.setAlignment(Element.ALIGN_RIGHT);
        signCell.addElement(companySignatory);

        var signatoryLabel = new Paragraph("Authorised Signatory", fontBodyBold);
        signatoryLabel.setAlignment(Element.ALIGN_RIGHT);
        signCell.addElement(signatoryLabel);

        tsTable.addCell(termsCell);
        tsTable.addCell(signCell);
        tsTable.writeSelectedRows(0, -1, left, termsTopY, canvas);
      } catch (Exception ignored) {
      }
    }
  }

  public static class InvoicePdfException extends RuntimeException {
    public InvoicePdfException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
