package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.ui.utils.AppConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StyleSheet("invoice-preview-dialog.css")
public class InvoicePreviewDialog extends Dialog {
  private final Invoice invoice;
  private final Customer billedCustomer;
  private final Customer shippedCustomer;
  private final List<Product> products;
  private final InvoicePdfService pdfService;
  private final String termsAndConditions;
  private final Map<Long, Product> productMap;

  public InvoicePreviewDialog(Invoice invoice, Customer billedCustomer, Customer shippedCustomer,
                              List<Product> products, InvoicePdfService pdfService,
                              @NonNull String termsAndConditions) {
    this.invoice = invoice;
    this.billedCustomer = billedCustomer;
    this.shippedCustomer = shippedCustomer != null ? shippedCustomer : billedCustomer;
    this.products = products;
    this.pdfService = pdfService;
    this.termsAndConditions = termsAndConditions.isBlank() ? AppConstants.DEFAULT_TERMS : termsAndConditions;

    this.productMap = new HashMap<>();
    for (Product p : products) productMap.put(p.getId(), p);

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
    title.addClassName("preview-dialog-title");

    var closeBtn = new Button(VaadinIcon.CLOSE.create(), _ -> close());
    closeBtn.addThemeVariants(ButtonVariant.TERTIARY);

    var header = new HorizontalLayout(title, closeBtn);
    header.addClassName("preview-dialog-header");
    header.setWidthFull();
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    return header;
  }

  // ── Invoice preview (mimics A4) ───────────────────────────────────
  private VerticalLayout buildPreviewContent() {
    var content = new VerticalLayout();
    content.addClassName("invoice-preview-dialog");
    content.setSpacing(false);
    content.setAlignItems(FlexComponent.Alignment.CENTER);

    var paper = new VerticalLayout();
    paper.addClassName("invoice-preview-paper");
    paper.setSpacing(false);

    paper.add(buildTopBar());
    paper.add(buildCenterHeader());
    paper.add(buildInvoiceMetaGrid());
    paper.add(buildBilledShippedRow());
    paper.add(buildItemsTableGrid());
    paper.add(buildFooterDetails());

    content.add(paper);
    return content;
  }

  private Div buildTopBar() {
    var gstin = new Span("GSTIN: 06CGLPP3030J1ZC");
    gstin.addClassName("preview-gstin");

    var original = new Span("Original Copy");
    original.addClassName("preview-original-copy");

    var row = new Div(gstin, original);
    row.addClassName("preview-top-bar");
    return row;
  }

  private VerticalLayout buildCenterHeader() {
    var logo = new Image("logo-rk-sm.jpg", "RK Logo");
    logo.setHeight("80px"); // Slightly larger for better visibility

    var taxInvoice = new Span("TAX INVOICE");
    taxInvoice.addClassName("preview-tax-invoice");

    var titleBlock = new VerticalLayout();
    titleBlock.addClassName("preview-title-block");
    titleBlock.setSpacing(false);
    titleBlock.setPadding(false);
    titleBlock.setAlignItems(FlexComponent.Alignment.CENTER);

    var companyName = new Span("R.K. SCIENTIFIC INDUSTRIES");
    companyName.addClassName("preview-company-name");

    var address = new Span("21A, BABYAL ROAD MAHESH NAGAR, AMBALA CANTT, Ambala, Haryana, 133001");
    address.addClassName("preview-company-address");

    var contact = new Span("Mob. : +917015539187, +918950959177  email : rkscientific.sales@gmail.com");
    contact.addClassName("preview-company-contact");

    titleBlock.add(companyName, address, contact);

    var mainRow = new HorizontalLayout(logo, titleBlock);
    mainRow.addClassName("preview-main-row");
    mainRow.setAlignItems(FlexComponent.Alignment.CENTER);
    mainRow.setSpacing(true);
    mainRow.setWidthFull();
    mainRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

    var header = new VerticalLayout(taxInvoice, mainRow);
    header.addClassName("preview-center-header");
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setSpacing(false);
    return header;
  }

  private Div buildInvoiceMetaGrid() {
    var grid = new Div();
    grid.addClassName("preview-meta-grid");

    var col1 = new VerticalLayout();
    col1.addClassNames("preview-meta-col", "preview-meta-col-left");
    col1.setSpacing(false);

    col1.add(metaItem("Invoice No.", invoice.getInvoiceNumber()));
    col1.add(metaItem("Dated", invoice.getInvoiceDate() != null ? invoice.getInvoiceDate()
                                                                  .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : ""));
    col1.add(metaItem("Place of Supply", invoice.getPlace() != null ? invoice.getPlace().name() : ""));
    col1.add(metaItem("Transport", invoice.getTransport() != null ? invoice.getTransport().name() : ""));

    var col2 = new VerticalLayout();
    col2.addClassName("preview-meta-col");
    col2.setSpacing(false);

    col2.add(metaItem("Vehicle No.", invoice.getVehicleNumber()));
    col2.add(metaItem("E-Way Bill No.", invoice.getEWayBillNumber()));
    col2.add(metaItem("NO. OF BOX", invoice.getPackageCount() != null ? invoice.getPackageCount().toString() : ""));

    grid.add(col1, col2);
    return grid;
  }

  private Div metaItem(String label, String value) {
    var lbl = new Span(label);
    lbl.addClassName("preview-meta-label");

    var val = new Span(": " + (value != null ? value : ""));
    val.addClassName("preview-meta-value");

    var item = new Div(lbl, val);
    item.addClassName("preview-meta-item");
    return item;
  }

  private Div buildBilledShippedRow() {
    var row = new Div();
    row.addClassName("preview-party-grid");

    var billedCol = buildPartyInfo("Billed to :", billedCustomer);
    billedCol.addClassName("preview-party-col-right");
    var shippedCol = buildPartyInfo("Shipped to :", shippedCustomer);
    row.add(billedCol, shippedCol);

    return row;
  }

  private VerticalLayout buildPartyInfo(String title, Customer c) {
    var layout = new VerticalLayout();
    layout.addClassName("preview-party-col");
    layout.setSpacing(false);

    var head = new Span(title);
    head.addClassName("preview-party-title");
    layout.add(head);

    if (c != null) {
      var name = new Span(c.getName().toUpperCase());
      name.addClassName("preview-party-name");

      var addr = new Span(c.getStreet() + ", " + c.getCity() + ", " + c.getState());
      addr.addClassName("preview-party-text");

      var pin = new Span("Pin Code:- " + c.getPostalCode());
      pin.addClassName("preview-party-text");

      var mob = new Div(new Span("Party Mobile No."), new Span(": " + (c.getPhone() != null ? c.getPhone() : "")));
      mob.addClassName("preview-party-pair");

      var gst = new Div(new Span("GSTIN / UIN"), new Span(": " + c.getGstin()));
      gst.addClassName("preview-party-pair");

      layout.add(name, addr, pin, mob, gst);
    }
    return layout;
  }

  private Div buildItemsTableGrid() {
    var table = new Div();
    table.addClassName("preview-items-table");

    // Header
    var header = new Div();
    header.addClassName("preview-items-header");

    header.add(headerCell("Sr No.", true));
    header.add(headerCell("Description of Goods", true));
    header.add(headerCell("HSN Code", true));
    header.add(headerCell("Qty", true));
    header.add(headerCell("Unit", true));
    header.add(headerCell("Price", true));
    header.add(headerCell("Gst", true));
    header.add(headerCell("Amount(₹)", false));
    table.add(header);

    // Rows
    if (invoice.getItems() != null) {
      int sr = 1;
      for (LineItem item : invoice.getItems()) {
        Product p = productMap.get(item.getProductId());
        var row = new Div();
        row.addClassName("preview-items-row");

        row.add(cell(String.valueOf(sr++), true, "center"));
        row.add(cell(p != null ? p.getName() : "Product #" + item.getProductId(), true, "left"));
        row.add(cell(p != null ? p.getHsnCode() : "", true, "center"));
        row.add(cell(String.valueOf(item.getQuantity()), true, "center"));
        row.add(cell(p != null ? p.getUnit().name() : "Nos", true, "center"));
        row.add(cell(fmt(item.getUnitPrice()), true, "right"));
        row.add(cell(item.getGstRate().stripTrailingZeros().toPlainString() + "%", true, "right"));

        BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
        BigDecimal excl = qty.multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
        row.add(cell(fmt(excl), false, "right"));

        table.add(row);
      }
    }

    // Fill empty space to maintain height if needed? No, just let it be.
    table.addClassName("preview-items-table-bordered");

    return table;
  }

  private Span headerCell(String text, boolean borderRight) {
    var s = new Span(text);
    s.addClassName("preview-header-cell");
    if (borderRight) s.addClassName("preview-cell-border-right");
    return s;
  }

  private Span cell(String text, boolean borderRight, String align) {
    var s = new Span(text);
    s.addClassName("preview-cell");
    s.addClassName("preview-align-" + align);
    if (borderRight) s.addClassName("preview-cell-border-right");
    return s;
  }

  private VerticalLayout buildFooterDetails() {
    var footer = new VerticalLayout();
    footer.addClassName("preview-details-footer");
    footer.setSpacing(false);
    footer.setPadding(false);

    // Bank Details
    var bankRow = new Div();
    bankRow.addClassName("preview-bank-row");

    var bankTitle = new Span("BANK DETAILS : ");
    bankTitle.addClassName("preview-bold");

    var bankInfo = new Span("HDFC BANK, AMBALA CANTT. A/C NO. : 50200049591048  IFSC CODE : HDFC0002562");
    bankInfo.addClassName("preview-bold");

    bankRow.add(bankTitle, bankInfo);
    footer.add(bankRow);

    // Terms and Signatures
    var bottomGrid = new Div();
    bottomGrid.addClassName("preview-bottom-grid");

    var termsCol = new VerticalLayout();
    termsCol.addClassName("preview-terms-col");
    termsCol.setSpacing(false);
    termsCol.setPadding(true);

    var termsTitle = new Span("Terms & Condition");
    termsTitle.addClassName("preview-terms-title");

    var termsText = new Span(termsAndConditions);
    termsText.addClassName("preview-terms-text");

    termsCol.add(termsTitle, termsText);

    var signCol = new VerticalLayout();
    signCol.addClassName("preview-sign-col");
    signCol.setSpacing(false);
    signCol.setPadding(false);

    var receiverSign = new Div(new Span("Receiver's Signature :"));
    receiverSign.addClassName("preview-receiver-sign");

    var authSign = new VerticalLayout();
    authSign.addClassName("preview-auth-sign");
    authSign.setAlignItems(FlexComponent.Alignment.END);
    authSign.setSpacing(false);
    authSign.setPadding(true);

    var forCo = new Span("For R.K. SCIENTIFIC INDUSTRIES");
    forCo.addClassName("preview-sign-strong");

    var space = new Div();
    space.setHeight("40px");

    var authLabel = new Span("Authorised Signatory");
    authLabel.addClassName("preview-sign-strong");

    authSign.add(forCo, space, authLabel);

    signCol.add(receiverSign, authSign);

    bottomGrid.add(termsCol, signCol);
    footer.add(bottomGrid);

    return footer;
  }

  // ── Dialog footer bar ──────────────────────────────────────────────
  private HorizontalLayout buildFooter() {
    // The anchor is hidden; the visible Download button triggers a programmatic click.
    var downloadAnchor = getDownloadAnchor();
    downloadAnchor.addClassName("preview-hidden-anchor");

    var downloadBtn = new Button("Download PDF", VaadinIcon.DOWNLOAD.create(),
        _ -> downloadAnchor.getElement().callJsFunction("click")
    );
    downloadBtn.addThemeVariants(ButtonVariant.PRIMARY);
    downloadBtn.setId("download-pdf-btn");

    var closeBtn = new Button("Close", _ -> close());
    closeBtn.setId("close-preview-btn");

    var footer = new HorizontalLayout(downloadAnchor, downloadBtn, closeBtn);
    footer.addClassName("preview-dialog-footer");
    footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    footer.setWidthFull();
    return footer;
  }

  private @NonNull Anchor getDownloadAnchor() {
    var downloadAnchor = new Anchor(
        DownloadHandler.fromInputStream(_ -> {
          try {
            byte[] pdfBytes = pdfService.generatePdf(invoice, billedCustomer, products, termsAndConditions);
            String filename = "invoice-" + (invoice.getInvoiceNumber() != null
                ? invoice.getInvoiceNumber() : "draft") + ".pdf";
            return new DownloadResponse(
                new ByteArrayInputStream(pdfBytes),
                filename,
                "application/pdf",
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
  private String fmt(BigDecimal v) {
    if (v == null) return "0.00";
    return String.format("%,.2f", v);
  }
}
