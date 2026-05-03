package com.rkscientificindustries.invoice.backend.invoice.pdf;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.format;
import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.formatRate;
import static com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfFormattingSupport.numberToWordsIndian;

@Component
class InvoicePdfTotalsRenderer {
  private final PdfCellFactory cellFactory;

  InvoicePdfTotalsRenderer(PdfCellFactory cellFactory) {
    this.cellFactory = cellFactory;
  }

  void render(Document document, InvoicePdfTotals totals, InvoicePdfFonts fonts) throws Exception {
    addCalculationSection(document, totals, fonts);
    addTaxSummarySection(document, totals, fonts);
    addAmountInWords(document, totals.grandTotal(), fonts);
  }

  private void addCalculationSection(Document document, InvoicePdfTotals totals, InvoicePdfFonts fonts) throws Exception {
    var table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{60, 10, 8, 22});

    addCalcRow(table, "Taxable Amount", "", "", format(totals.taxableTotal()), fonts);

    if (totals.taxByRate().isEmpty()) {
      addCalcRow(table, "Add : IGST", "@", "0%", format(BigDecimal.ZERO), fonts);
    } else {
      boolean first = true;
      for (Map.Entry<BigDecimal, BigDecimal> entry : totals.taxByRate().entrySet()) {
        String label = first ? "Add : IGST" : "";
        addCalcRow(table, label, "@", formatRate(entry.getKey()) + "%", format(entry.getValue()), fonts);
        first = false;
      }
    }

    addCalcRow(table, "Transportation", "", "", format(BigDecimal.ZERO), fonts);
    addCalcRow(table, "Packing", "", "", format(BigDecimal.ZERO), fonts);
    addCalcRow(table, "Roundoff", "", "", format(totals.roundOff()), fonts);

    var grandLabel = cellFactory.boxedCell("Grand Total", fonts.bodyBold(), Element.ALIGN_CENTER, 3f);
    grandLabel.setColspan(3);

    var grandValue = cellFactory.boxedCell(format(totals.grandTotal()), fonts.bodyBold(), Element.ALIGN_RIGHT, 3f);

    table.addCell(grandLabel);
    table.addCell(grandValue);

    table.setSpacingAfter(0f);
    document.add(table);
  }

  private void addTaxSummarySection(Document document, InvoicePdfTotals totals, InvoicePdfFonts fonts) throws Exception {
    var table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setWidths(new float[]{14, 18, 18, 18});

    addSummaryHeaderCell(table, "Tax Rate", fonts);
    addSummaryHeaderCell(table, "Taxable Amt.", fonts);
    addSummaryHeaderCell(table, "IGST Amt.", fonts);
    addSummaryHeaderCell(table, "Total Tax", fonts);

    if (totals.taxByRate().isEmpty()) {
      addSummaryDataCell(table, "0%", fonts);
      addSummaryDataCell(table, format(BigDecimal.ZERO), fonts);
      addSummaryDataCell(table, format(BigDecimal.ZERO), fonts);
      addSummaryDataCell(table, format(BigDecimal.ZERO), fonts);
    } else {
      List<String> rates = new ArrayList<>();
      for (BigDecimal rate : totals.taxByRate().keySet()) {
        rates.add(formatRate(rate) + "%");
      }
      addSummaryDataCell(table, String.join(", ", rates), fonts);
      addSummaryDataCell(table, format(totals.taxableTotal()), fonts);
      addSummaryDataCell(table, format(totals.taxTotal()), fonts);
      addSummaryDataCell(table, format(totals.taxTotal()), fonts);
    }

    table.setSpacingAfter(0f);
    document.add(table);
  }

  private void addAmountInWords(Document document, BigDecimal grandTotal, InvoicePdfFonts fonts) throws Exception {
    String words = "Rupees " + numberToWordsIndian(grandTotal.abs().setScale(0, RoundingMode.HALF_UP).longValue()) + " Only";
    var paragraph = new Paragraph(words, fonts.bodyBold());
    paragraph.setSpacingBefore(2f);
    paragraph.setSpacingAfter(2f);
    document.add(paragraph);
  }

  private void addCalcRow(PdfPTable table, String label, String at, String percent, String value, InvoicePdfFonts fonts) {
    table.addCell(cellFactory.borderlessCell(label, fonts.body(), Element.ALIGN_CENTER, 3f));
    table.addCell(cellFactory.borderlessCell(at, fonts.body(), Element.ALIGN_CENTER, 3f));
    table.addCell(cellFactory.borderlessCell(percent, fonts.body(), Element.ALIGN_CENTER, 3f));
    table.addCell(cellFactory.borderlessCell(value, fonts.body(), Element.ALIGN_RIGHT, 3f));
  }

  private void addSummaryHeaderCell(PdfPTable table, String text, InvoicePdfFonts fonts) {
    table.addCell(cellFactory.borderlessCell(text, fonts.bodyBold(), Element.ALIGN_LEFT, 3f));
  }

  private void addSummaryDataCell(PdfPTable table, String text, InvoicePdfFonts fonts) {
    table.addCell(cellFactory.borderlessCell(text, fonts.body(), Element.ALIGN_LEFT, 3f));
  }
}
