package com.rkscientificindustries.invoice.ui.invoices.newflow;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InvoicePreviewDialog extends Dialog {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final String CURRENCY = "₹";

  private final Invoice invoice;
  private final Customer customer;
  private final List<Product> products;
  private final InvoicePdfService pdfService;

  public InvoicePreviewDialog(Invoice invoice, Customer customer,
                              List<Product> products, InvoicePdfService pdfService) {
    this.invoice = invoice;
    this.customer = customer;
    this.products = products;
    this.pdfService = pdfService;

    setWidth("800px");
    setMaxHeight("90vh");
    setResizable(true);
    setDraggable(true);
    getElement().getThemeList().add("no-padding");

    add(buildHeader(), buildPreviewContent(), buildFooter());
  }

  // ── Dialog header ──────────────────────────────────────────────────
  private HorizontalLayout buildHeader() {
    var title = new H3("Invoice Preview");
    title.getStyle().set("margin", "0");

    var closeBtn = new Button(VaadinIcon.CLOSE.create(), _ -> close());
    closeBtn.addThemeVariants(ButtonVariant.TERTIARY);

    var header = new HorizontalLayout(title, closeBtn);
    header.setWidthFull();
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    header.getStyle()
        .set("padding", "16px")
        .set("border-bottom", "1px solid #e0e0e0");
    return header;
  }

  // ── Invoice preview (mimics PDF) ───────────────────────────────────
  private VerticalLayout buildPreviewContent() {
    var content = new VerticalLayout();
    content.getStyle()
        .set("padding", "24px")
        .set("background", "white")
        .set("overflow-y", "auto");
    content.setSpacing(false);

    // Company + Invoice header
    var companyName = new H2("RK Scientific Industries");
    companyName.getStyle().set("margin-bottom", "0");

    var companyInfo = new Span("GSTIN: 07AABCR1234H1Z5 | Phone: +91 98100 00000");
    companyInfo.getStyle().set("color", "#666").set("font-size", "13px");

    var invoiceLabel = styledSpan("INVOICE", "18px", "#1a1a1a", true);

    var topRow = new HorizontalLayout(new VerticalLayout(companyName, companyInfo), invoiceLabel);
    topRow.setWidthFull();
    topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    topRow.setAlignItems(FlexComponent.Alignment.START);
    content.add(topRow);
    content.add(new Hr());

    // Meta row
    var invNo = styledPair("Invoice No:", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "—");
    var invDate = styledPair("Invoice Date:", invoice.getInvoiceDate() != null ? invoice.getInvoiceDate()
        .format(DATE_FMT) : "—");
    var dueDate = styledPair("Due Date:", invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FMT) : "—");

    var metaRow = new HorizontalLayout(invNo, invDate, dueDate);
    metaRow.setSpacing(true);
    metaRow.getStyle().set("margin", "12px 0");
    content.add(metaRow);

    // Billed To
    if (customer != null) {
      var billedToLabel = styledSpan("Billed To", "11px", "#999", false);
      var billedName = styledSpan(customer.getName(), "14px", "#1a1a1a", true);
      var billedAddr = styledSpan(
          customer.getStreet() + ", " + customer.getCity() + ", " + customer.getState() + " – " + customer.getPostalCode() +
              "\nGSTIN: " + customer.getGstin(),
          "13px", "#555", false
      );
      billedAddr.getStyle().set("white-space", "pre-line");
      content.add(new VerticalLayout(billedToLabel, billedName, billedAddr) {{
        setPadding(false);
        setSpacing(false);
        getStyle().set("margin", "8px 0 16px 0");
      }});
    }

    // Line items table
    content.add(buildLineItemsTable());

    // Totals
    content.add(buildTotalsBlock());

    // Terms
    if (invoice.getTermsAndConditions() != null && !invoice.getTermsAndConditions().isBlank()) {
      content.add(new Hr());
      content.add(styledSpan("Terms & Conditions", "12px", "#555", true));
      var tnc = styledSpan(invoice.getTermsAndConditions(), "12px", "#555", false);
      tnc.getStyle().set("white-space", "pre-wrap");
      content.add(tnc);
    }

    return content;
  }

  private Div buildLineItemsTable() {
    var table = new Div();
    table.getStyle()
        .set("width", "100%")
        .set("border-collapse", "collapse")
        .set("margin", "16px 0");

    // Header row
    table.add(buildTableRow(
        true,
        "Description", "Qty", "Unit Price", "Tax %", "Tax Excl.", "Tax Incl."
    ));

    Map<Long, Product> productMap = new java.util.HashMap<>();
    for (Product p : products) productMap.put(p.getId(), p);

    if (invoice.getItems() != null) {
      for (LineItem item : invoice.getItems()) {
        Product p = productMap.get(item.getProductId());
        String desc = p != null ? p.getName() : "Product #" + item.getProductId();
        BigDecimal qty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0);
        BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal rate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
        BigDecimal excl = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = excl.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal incl = excl.add(tax);

        table.add(buildTableRow(false,
            desc,
            qty.toPlainString(),
            CURRENCY + " " + fmt(price),
            rate.stripTrailingZeros().toPlainString() + "%",
            CURRENCY + " " + fmt(excl),
            CURRENCY + " " + fmt(incl)
        ));
      }
    }
    return table;
  }

  private Div buildTableRow(boolean isHeader, String... values) {
    var row = new Div();
    row.getStyle()
        .set("display", "grid")
        .set("grid-template-columns", "3fr 1fr 1.5fr 1fr 1.5fr 1.5fr")
        .set("border-bottom", "1px solid #e0e0e0")
        .set("padding", "6px 4px")
        .set("background", isHeader ? "#f5f5f5" : "white");

    for (int i = 0; i < values.length; i++) {
      var cell = new Span(values[i]);
      cell.getStyle()
          .set("font-size", "13px")
          .set("padding", "0 4px")
          .set("font-weight", isHeader ? "bold" : "normal")
          .set("text-align", i > 1 ? "right" : "left");
      row.add(cell);
    }
    return row;
  }

  private Div buildTotalsBlock() {
    BigDecimal untaxed = BigDecimal.ZERO;
    Map<String, BigDecimal> slabs = new LinkedHashMap<>();

    if (invoice.getItems() != null) {
      for (LineItem item : invoice.getItems()) {
        BigDecimal qty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0);
        BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal rate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
        BigDecimal excl = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = excl.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        untaxed = untaxed.add(excl);
        if (rate.compareTo(BigDecimal.ZERO) > 0) {
          slabs.merge("GST " + rate.stripTrailingZeros().toPlainString() + "%", tax, BigDecimal::add);
        }
      }
    }
    BigDecimal total = untaxed;
    for (BigDecimal v : slabs.values()) total = total.add(v);

    var block = new Div();
    block.getStyle()
        .set("margin-left", "auto")
        .set("width", "300px")
        .set("margin-top", "12px");

    block.add(totalLine("Untaxed Amount", untaxed, false));
    for (Map.Entry<String, BigDecimal> e : slabs.entrySet()) {
      block.add(totalLine(e.getKey(), e.getValue(), false));
    }

    var sep = new Div();
    sep.getStyle().set("border-top", "1px solid #ccc").set("margin", "6px 0");
    block.add(sep);
    block.add(totalLine("Total", total, true));
    return block;
  }

  private Div totalLine(String label, BigDecimal value, boolean bold) {
    var lblSpan = new Span(label + ":");
    var valSpan = new Span(CURRENCY + " " + fmt(value));
    if (bold) {
      lblSpan.getStyle().set("font-weight", "bold");
      valSpan.getStyle().set("font-weight", "bold").set("color", "#2e7d32");
    } else {
      lblSpan.getStyle().set("color", "#555");
    }
    lblSpan.getStyle().set("font-size", "13px");
    valSpan.getStyle().set("font-size", "13px");

    var row = new Div(lblSpan, valSpan);
    row.getStyle()
        .set("display", "flex")
        .set("justify-content", "space-between")
        .set("padding", "3px 0");
    return row;
  }

  // ── Dialog footer bar ──────────────────────────────────────────────
  private HorizontalLayout buildFooter() {
    // Use DownloadHandler (non-deprecated Vaadin 25 API) attached directly to an Anchor.
    // The anchor is hidden; the visible Download button triggers a programmatic click.
    var downloadAnchor = getDownloadAnchor();
    downloadAnchor.getStyle().set("display", "none");

    var downloadBtn = new Button("Download PDF", VaadinIcon.DOWNLOAD.create(),
        _ -> downloadAnchor.getElement().callJsFunction("click")
    );
    downloadBtn.addThemeVariants(ButtonVariant.PRIMARY);
    downloadBtn.setId("download-pdf-btn");

    var closeBtn = new Button("Close", _ -> close());
    closeBtn.setId("close-preview-btn");

    var footer = new HorizontalLayout(downloadAnchor, downloadBtn, closeBtn);
    footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    footer.setWidthFull();
    footer.getStyle()
        .set("padding", "16px")
        .set("border-top", "1px solid #e0e0e0");
    return footer;
  }

  private @NonNull Anchor getDownloadAnchor() {
    var downloadAnchor = new Anchor(
        DownloadHandler.fromInputStream(_ -> {
          try {
            byte[] pdfBytes = pdfService.generatePdf(invoice, customer, products);
            String filename = "invoice-" + (invoice.getInvoiceNumber() != null
                ? invoice.getInvoiceNumber() : "draft") + ".pdf";
            // Constructor: new DownloadResponse(InputStream, mimeType, fileName, size)
            return new DownloadResponse(
                new java.io.ByteArrayInputStream(pdfBytes),
                "application/pdf",
                filename,
                pdfBytes.length
            );
          } catch (Exception e) {
            return DownloadResponse.error(500);
          }
        }), ""
    );
    downloadAnchor.setId("download-pdf-anchor");
    return downloadAnchor;
  }

  // ── Helpers ────────────────────────────────────────────────────────
  private Span styledSpan(String text, String fontSize, String color, boolean bold) {
    var span = new Span(text);
    span.getStyle()
        .set("font-size", fontSize)
        .set("color", color)
        .set("font-weight", bold ? "bold" : "normal");
    return span;
  }

  private Div styledPair(String label, String value) {
    var lbl = new Span(label + " ");
    lbl.getStyle().set("color", "#999").set("font-size", "12px");
    var val = new Span(value);
    val.getStyle().set("font-weight", "bold").set("font-size", "13px");
    var pair = new Div(lbl, val);
    pair.getStyle().set("display", "flex").set("flex-direction", "column");
    return pair;
  }

  private String fmt(BigDecimal v) {
    return String.format("%,.2f", v);
  }
}
