package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.ui.utils.AppConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

  // ── Invoice preview (mimics A4) ───────────────────────────────────
  private VerticalLayout buildPreviewContent() {
    var content = new VerticalLayout();
    content.getStyle()
        .set("padding", "20px")
        .set("background", "#f5f7f9")
        .set("overflow-y", "auto")
        .set("min-height", "600px");
    content.setSpacing(false);
    content.setAlignItems(FlexComponent.Alignment.CENTER);

    var paper = new VerticalLayout();
    paper.getStyle()
        .set("width", "100%")
        .set("max-width", "780px")
        .set("background", "white")
        .set("border", "1px solid #333")
        .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
        .set("padding", "0")
        .set("color", "black")
        .set("font-family", "'Inter', system-ui, sans-serif");
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
    gstin.getStyle().set("font-weight", "bold").set("font-size", "11px");

    var original = new Span("Original Copy");
    original.getStyle().set("font-size", "11px");

    var row = new Div(gstin, original);
    row.getStyle()
        .set("box-sizing", "border-box")
        .set("display", "flex")
        .set("justify-content", "space-between")
        .set("width", "100%")
        .set("padding", "2px 10px");
    return row;
  }

  private VerticalLayout buildCenterHeader() {
    var logo = new Image("logo-rk-sm.jpg", "RK Logo");
    logo.setHeight("80px"); // Slightly larger for better visibility

    var taxInvoice = new Span("TAX INVOICE");
    taxInvoice.getStyle()
        .set("text-decoration", "underline")
        .set("font-weight", "bold")
        .set("font-size", "12px")
        .set("margin-bottom", "8px");

    var titleBlock = new VerticalLayout();
    titleBlock.setSpacing(false);
    titleBlock.setPadding(false);
    titleBlock.setAlignItems(FlexComponent.Alignment.CENTER);

    var companyName = new Span("R.K. SCIENTIFIC INDUSTRIES");
    companyName.getStyle()
        .set("font-size", "28px")
        .set("font-weight", "800")
        .set("letter-spacing", "1px")
        .set("margin-bottom", "2px");

    var address = new Span("21A, BABYAL ROAD MAHESH NAGAR, AMBALA CANTT, Ambala, Haryana, 133001");
    address.getStyle().set("font-size", "12px");

    var contact = new Span("Mob. : +917015539187, +918950959177  email : rkscientific.sales@gmail.com");
    contact.getStyle().set("font-size", "11px").set("font-weight", "bold");

    titleBlock.add(companyName, address, contact);

    var mainRow = new HorizontalLayout(logo, titleBlock);
    mainRow.setAlignItems(FlexComponent.Alignment.CENTER);
    mainRow.setSpacing(true);
    mainRow.setWidthFull();
    mainRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

    var header = new VerticalLayout(taxInvoice, mainRow);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setSpacing(false);
    header.getStyle()
        .set("padding", "0px 10px")
        .set("border-bottom", "1px solid #333");
    return header;
  }

  private Div buildInvoiceMetaGrid() {
    var grid = new Div();
    grid.getStyle()
        .set("display", "grid")
        .set("grid-template-columns", "1fr 1fr")
        .set("width", "100%")
        .set("border-bottom", "1px solid #333");

    var col1 = new VerticalLayout();
    col1.setSpacing(false);
    col1.getStyle()
        .set("padding", "0px 10px")
        .set("border-right", "1px solid #333");

    col1.add(metaItem("Invoice No.", invoice.getInvoiceNumber()));
    col1.add(metaItem("Dated", invoice.getInvoiceDate() != null ? invoice.getInvoiceDate()
                                                                  .format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : ""));
    col1.add(metaItem("Place of Supply", invoice.getPlace() != null ? invoice.getPlace().name() : ""));
    col1.add(metaItem("Transport", invoice.getTransport() != null ? invoice.getTransport().name() : ""));

    var col2 = new VerticalLayout();
    col2.setSpacing(false);
    col2.getStyle()
        .set("padding", "0px 10px");

    col2.add(metaItem("Vehicle No.", invoice.getVehicleNumber()));
    col2.add(metaItem("E-Way Bill No.", invoice.getEWayBillNumber()));
    col2.add(metaItem("NO. OF BOX", invoice.getPackageCount() != null ? invoice.getPackageCount().toString() : ""));

    grid.add(col1, col2);
    return grid;
  }

  private Div metaItem(String label, String value) {
    var lbl = new Span(label);
    lbl.getStyle().set("width", "120px").set("font-size", "11px");

    var val = new Span(": " + (value != null ? value : ""));
    val.getStyle().set("font-size", "11px").set("font-weight", "bold");

    var item = new Div(lbl, val);
    item.getStyle()
        .set("display", "flex")
        .set("padding-right", "10px")
        .set("width", "100%");
    return item;
  }

  private Div buildBilledShippedRow() {
    var row = new Div();
    row.getStyle()
        .set("display", "grid")
        .set("grid-template-columns", "1fr 1fr")
        .set("width", "100%")
        .set("border-bottom", "1px solid #333");

    row.add(buildPartyInfo("Billed to :", billedCustomer));
    var shippedCol = buildPartyInfo("Shipped to :", shippedCustomer);
    shippedCol.getStyle().set("border-left", "1px solid #333");
    row.add(shippedCol);

    return row;
  }

  private VerticalLayout buildPartyInfo(String title, Customer c) {
    var layout = new VerticalLayout();
    layout.setSpacing(false);
    layout.setPadding(true);
    layout.getStyle().set("padding", "5px 10px");

    var head = new Span(title);
    head.getStyle().set("font-weight", "bold").set("font-size", "12px");
    layout.add(head);

    if (c != null) {
      var name = new Span(c.getName().toUpperCase());
      name.getStyle().set("font-weight", "bold").set("font-size", "12px").set("margin-top", "2px");

      var addr = new Span(c.getStreet() + ", " + c.getCity() + ", " + c.getState());
      addr.getStyle().set("font-size", "11px");

      var pin = new Span("Pin Code:- " + c.getPostalCode());
      pin.getStyle().set("font-size", "11px");

      var mob = new Div(new Span("Party Mobile No."), new Span(": " + (c.getPhone() != null ? c.getPhone() : "")));
      mob.getStyle().set("display", "flex").set("justify-content", "space-between")
          .set("font-size", "11px");

      var gst = new Div(new Span("GSTIN / UIN"), new Span(": " + c.getGstin()));
      gst.getStyle().set("display", "flex").set("justify-content", "space-between").set("font-size", "11px");

      layout.add(name, addr, pin, mob, gst);
    }
    return layout;
  }

  private Div buildItemsTableGrid() {
    var table = new Div();
    table.getStyle()
        .set("width", "100%")
        .set("display", "flex")
        .set("flex-direction", "column");

    // Header
    var header = new Div();
    header.getStyle()
        .set("display", "grid")
        .set("grid-template-columns", "40px 1fr 80px 50px 60px 80px 50px 100px")
        .set("border-bottom", "1px solid #333")
        .set("font-weight", "bold")
        .set("font-size", "11px")
        .set("text-align", "center");

    header.add(headerCell("Sr No.", true));
    header.add(headerCell("Description of Goods", true));
    header.add(headerCell("HSN/SAC Code", true));
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
        row.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "40px 1fr 80px 50px 60px 80px 50px 100px")
            .set("font-size", "11px");

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
    table.getStyle().set("border-bottom", "1px solid #333");

    return table;
  }

  private Span headerCell(String text, boolean borderRight) {
    var s = new Span(text);
    s.getStyle().set("padding", "4px 2px");
    if (borderRight) s.getStyle().set("border-right", "1px solid #333");
    return s;
  }

  private Span cell(String text, boolean borderRight, String align) {
    var s = new Span(text);
    s.getStyle().set("padding", "0px 5px").set("text-align", align);
    if (borderRight) s.getStyle().set("border-right", "1px solid #333");
    return s;
  }

  private VerticalLayout buildFooterDetails() {
    var footer = new VerticalLayout();
    footer.setSpacing(false);
    footer.setPadding(false);

    // Bank Details
    var bankRow = new Div();
    bankRow.getStyle()
        .set("width", "100%")
        .set("padding", "5px 10px")
        .set("border-bottom", "1px solid #333")
        .set("font-size", "11px");

    var bankTitle = new Span("BANK DETAILS : ");
    bankTitle.getStyle().set("font-weight", "bold");

    var bankInfo = new Span("HDFC BANK, AMBALA CANTT. A/C NO. : 50200049591048  IFSC CODE : HDFC0002562");
    bankInfo.getStyle().set("font-weight", "bold");

    bankRow.add(bankTitle, bankInfo);
    footer.add(bankRow);

    // Terms and Signatures
    var bottomGrid = new Div();
    bottomGrid.getStyle()
        .set("display", "grid")
        .set("grid-template-columns", "1fr 1fr")
        .set("width", "100%");

    var termsCol = new VerticalLayout();
    termsCol.setSpacing(false);
    termsCol.setPadding(true);
    termsCol.getStyle()
        .set("padding", "5px 10px")
        .set("border-right", "1px solid #333");

    var termsTitle = new Span("Terms & Condition");
    termsTitle.getStyle()
        .set("font-weight", "bold")
        .set("font-size", "11px")
        .set("text-decoration", "underline");

    var termsText = new Span(termsAndConditions);
    termsText.getStyle()
        .set("font-size", "10px")
        .set("white-space", "pre-wrap");

    termsCol.add(termsTitle, termsText);

    var signCol = new VerticalLayout();
    signCol.setSpacing(false);
    signCol.setPadding(false);

    var receiverSign = new Div(new Span("Receiver's Signature :"));
    receiverSign.getStyle()
        .set("height", "60px")
        .set("padding", "5px 10px")
        .set("font-size", "11px");

    var authSign = new VerticalLayout();
    authSign.setAlignItems(FlexComponent.Alignment.END);
    authSign.setSpacing(false);
    authSign.setPadding(true);
    authSign.getStyle()
        .set("border-top", "1px solid #333")
        .set("padding", "5px 10px");

    var forCo = new Span("For R.K. SCIENTIFIC INDUSTRIES");
    forCo.getStyle()
        .set("font-weight", "bold")
        .set("font-size", "11px");

    var space = new Div();
    space.setHeight("40px");

    var authLabel = new Span("Authorised Signatory");
    authLabel.getStyle()
        .set("font-weight", "bold")
        .set("font-size", "11px");

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
