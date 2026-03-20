package com.rkscientificindustries.invoice.backend.invoice;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.product.Product;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class InvoicePdfService {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final String CURRENCY = "₹";

  // Company details — can be extracted to @ConfigurationProperties later
  private static final String COMPANY_NAME = "RK Scientific Industries";
  private static final String COMPANY_ADDRESS = "21 A, Mahesh Nagar, Near Sheetla Mata Mandir";
  private static final String COMPANY_CITY = "Ambala Cantt – 133001";
  private static final String COMPANY_GSTIN = "07AABCR1234H1Z5";
  private static final String COMPANY_PHONE = "+91 98100 00000";

  // Fonts
  private final Font fontTitle = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
  private final Font fontHeader = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
  private final Font fontNormal = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
  private final Font fontSmall = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 100, 100));
  private final Font fontBold = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
  private final Font fontTotalLbl = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
  private final Font fontTotalVal = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(30, 100, 50));

  public byte[] generatePdf(Invoice invoice, Customer customer, List<Product> products) {
    try (var out = new ByteArrayOutputStream()) {
      var document = new Document(PageSize.A4, 36, 36, 54, 36);
      PdfWriter.getInstance(document, out);
      document.open();

      // ── Company header ──────────────────────────────────────
      var companyTable = new PdfPTable(2);
      companyTable.setWidthPercentage(100);
      companyTable.setWidths(new float[]{60, 40});
      companyTable.setSpacingAfter(12);

      var nameCell = new PdfPCell(new Phrase(COMPANY_NAME, fontTitle));
      nameCell.setBorder(Rectangle.NO_BORDER);
      companyTable.addCell(nameCell);

      var invoiceLabelCell = new PdfPCell(new Phrase("INVOICE", fontTitle));
      invoiceLabelCell.setBorder(Rectangle.NO_BORDER);
      invoiceLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      companyTable.addCell(invoiceLabelCell);

      addCompanyInfoCell(companyTable, COMPANY_ADDRESS + "\n" + COMPANY_CITY + "\nGSTIN: " + COMPANY_GSTIN + "\nPhone: " + COMPANY_PHONE);

      var invoiceMetaCell = buildRightAlignedMetaCell(invoice);
      companyTable.addCell(invoiceMetaCell);

      document.add(companyTable);

      // Divider
      var divider = new Paragraph(" ");
      divider.setSpacingBefore(2);
      divider.setSpacingAfter(2);
      document.add(new Chunk(new org.openpdf.text.pdf.draw.LineSeparator(0.5f, 100, Color.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));

      // ── Billed To ────────────────────────────────────────────
      var billedTable = new PdfPTable(1);
      billedTable.setWidthPercentage(50);
      billedTable.setHorizontalAlignment(Element.ALIGN_LEFT);
      billedTable.setSpacingBefore(10);
      billedTable.setSpacingAfter(14);

      var billedLabel = new PdfPCell(new Phrase("Bill To", fontSmall));
      billedLabel.setBorder(Rectangle.NO_BORDER);
      billedTable.addCell(billedLabel);

      String customerText = customer != null
          ? customer.getName() + "\n" + customer.getStreet() + "\n" + customer.getCity() + ", " + customer.getState() + " – " + customer.getPostalCode() + "\nGSTIN: " + customer.getGstin()
          : "—";

      var billedCell = new PdfPCell(new Phrase(customerText, fontNormal));
      billedCell.setBorder(Rectangle.NO_BORDER);
      billedTable.addCell(billedCell);

      document.add(billedTable);

      // ── Line Items Table ─────────────────────────────────────
      var itemsTable = new PdfPTable(6);
      itemsTable.setWidthPercentage(100);
      itemsTable.setWidths(new float[]{35, 10, 15, 10, 15, 15});
      itemsTable.setSpacingAfter(4);

      addTableHeader(itemsTable, "Description", "Qty", "Unit Price", "Tax %", "Tax Excl.", "Tax Incl.");

      // product lookup map
      Map<Long, Product> productMap = new java.util.HashMap<>();
      for (Product p : products) {
        productMap.put(p.getId(), p);
      }

      BigDecimal untaxedTotal = BigDecimal.ZERO;
      // slab → amount  (e.g. "5%" → sum)
      Map<String, BigDecimal> taxSlabs = new TreeMap<>();

      for (LineItem item : invoice.getItems()) {
        Product p = productMap.get(item.getProductId());
        String desc = p != null ? p.getName() : "Product #" + item.getProductId();
        BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
        BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal gstRate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
        BigDecimal taxExcl = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmt = taxExcl.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxIncl = taxExcl.add(taxAmt);

        untaxedTotal = untaxedTotal.add(taxExcl);
        String slab = formatRate(gstRate) + "%";
        taxSlabs.merge(slab, taxAmt, BigDecimal::add);

        addDataRow(itemsTable, desc,
            qty.toPlainString(),
            CURRENCY + " " + format(price),
            formatRate(gstRate) + "%",
            CURRENCY + " " + format(taxExcl),
            CURRENCY + " " + format(taxIncl));
      }
      document.add(itemsTable);

      // ── Totals block ─────────────────────────────────────────
      var totalsTable = new PdfPTable(2);
      totalsTable.setWidthPercentage(45);
      totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
      totalsTable.setWidths(new float[]{60, 40});
      totalsTable.setSpacingBefore(6);
      totalsTable.setSpacingAfter(14);

      addTotalRow(totalsTable, "Untaxed Amount:", CURRENCY + " " + format(untaxedTotal), fontNormal, fontNormal);

      BigDecimal grandTotal = untaxedTotal;
      for (Map.Entry<String, BigDecimal> entry : taxSlabs.entrySet()) {
        addTotalRow(totalsTable, "GST " + entry.getKey() + ":", CURRENCY + " " + format(entry.getValue()), fontNormal, fontNormal);
        grandTotal = grandTotal.add(entry.getValue());
      }

      // Separator row
      var sepCell = new PdfPCell(new Phrase(""));
      sepCell.setColspan(2);
      sepCell.setBorderWidthTop(0.5f);
      sepCell.setBorderWidthBottom(0);
      sepCell.setBorderWidthLeft(0);
      sepCell.setBorderWidthRight(0);
      sepCell.setBorderColor(Color.LIGHT_GRAY);
      sepCell.setFixedHeight(2);
      totalsTable.addCell(sepCell);

      addTotalRow(totalsTable, "Total:", CURRENCY + " " + format(grandTotal), fontTotalLbl, fontTotalVal);

      document.add(totalsTable);

      // ── Terms & Conditions ────────────────────────────────────
      if (invoice.getTermsAndConditions() != null && !invoice.getTermsAndConditions().isBlank()) {
        document.add(new Chunk(new org.openpdf.text.pdf.draw.LineSeparator(0.5f, 100, Color.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));
        var tnc = new Paragraph("Terms & Conditions", fontBold);
        tnc.setSpacingBefore(8);
        tnc.setSpacingAfter(4);
        document.add(tnc);
        document.add(new Paragraph(invoice.getTermsAndConditions(), fontNormal));
      }

      document.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new InvoicePdfException("Failed to generate invoice PDF", e);
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────

  private void addCompanyInfoCell(PdfPTable table, String text) {
    var cell = new PdfPCell(new Phrase(text, fontSmall));
    cell.setBorder(Rectangle.NO_BORDER);
    cell.setVerticalAlignment(Element.ALIGN_TOP);
    table.addCell(cell);
  }

  private PdfPCell buildRightAlignedMetaCell(Invoice invoice) {
    var sb = new StringBuilder();
    sb.append("Invoice No: ").append(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A")
        .append("\n");
    if (invoice.getInvoiceDate() != null) {
      sb.append("Invoice Date: ").append(invoice.getInvoiceDate().format(DATE_FMT)).append("\n");
    }
    if (invoice.getDueDate() != null) {
      sb.append("Due Date: ").append(invoice.getDueDate().format(DATE_FMT)).append("\n");
    }
    var cell = new PdfPCell(new Phrase(sb.toString(), fontSmall));
    cell.setBorder(Rectangle.NO_BORDER);
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    cell.setVerticalAlignment(Element.ALIGN_TOP);
    return cell;
  }

  private void addTableHeader(PdfPTable table, String... headers) {
    var headerBg = new Color(240, 240, 240);
    for (String h : headers) {
      var cell = new PdfPCell(new Phrase(h, fontHeader));
      cell.setBackgroundColor(headerBg);
      cell.setPadding(5);
      cell.setBorderColor(new Color(200, 200, 200));
      table.addCell(cell);
    }
  }

  private void addDataRow(PdfPTable table, String... values) {
    for (int i = 0; i < values.length; i++) {
      var cell = new PdfPCell(new Phrase(values[i], fontNormal));
      cell.setPadding(4);
      cell.setBorderColor(new Color(220, 220, 220));
      if (i > 1) { // numeric columns right-align
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      }
      table.addCell(cell);
    }
  }

  private void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
    var labelCell = new PdfPCell(new Phrase(label, labelFont));
    labelCell.setBorder(Rectangle.NO_BORDER);
    labelCell.setPaddingTop(3);
    table.addCell(labelCell);

    var valueCell = new PdfPCell(new Phrase(value, valueFont));
    valueCell.setBorder(Rectangle.NO_BORDER);
    valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    valueCell.setPaddingTop(3);
    table.addCell(valueCell);
  }

  private String format(BigDecimal value) {
    return String.format("%,.2f", value);
  }

  private String formatRate(BigDecimal rate) {
    if (rate == null) return "0";
    return rate.stripTrailingZeros().toPlainString();
  }

  public static class InvoicePdfException extends RuntimeException {
    public InvoicePdfException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
