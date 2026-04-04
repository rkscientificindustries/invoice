package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceService;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceStatus;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductService;
import com.rkscientificindustries.invoice.ui.MainLayout;
import com.rkscientificindustries.invoice.ui.utils.InvoiceUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.rkscientificindustries.invoice.ui.utils.InvoiceUtils.showNotification;

@PageTitle("Invoice")
@Route(value = "invoices/details/:invoiceId?", layout = MainLayout.class)
public class InvoiceView extends VerticalLayout implements BeforeEnterObserver {
  // ── Services ─────────────────────────────────────────────────────
  private final InvoiceService invoiceService;
  private final CustomerService customerService;
  private final ProductService productService;
  private final InvoicePdfService pdfService;
  // ── Line items ─────────────────────────────────────────────────────
  private final List<LineItemRow> lineItemRows = new ArrayList<>();
  // ── State ─────────────────────────────────────────────────────────
  private Invoice currentInvoice;
  private List<Customer> allCustomers;
  private List<Product> allProducts;
  // ── Header fields ─────────────────────────────────────────────────
  private ComboBox<Customer> customerCombo;
  private Div customerAddressBlock;
  private DatePicker invoiceDatePicker;
  private TextField invoiceNumberField;
  private DatePicker dueDatePicker;
  private Select<DueTerm> dueTermSelect;
  private Grid<LineItemRow> lineGrid;

  // ── Other info ─────────────────────────────────────────────────────
  private Select<Invoice.Transport> transportSelect;
  private TextField courierNameField;
  private TextField vehicleNumberField;
  private TextField eWayBillField;

  // ── Footer ─────────────────────────────────────────────────────────
  private TextArea termsArea;

  // ── Totals display ─────────────────────────────────────────────────
  private VerticalLayout totalsSection;

  // ── Action bar ─────────────────────────────────────────────────────
  private Badge statusBadge;
  private Button finalizeBtn;
  private Button revertBtn;
  private Button saveDraftBtn;

  public InvoiceView(InvoiceService invoiceService, CustomerService customerService,
                     ProductService productService, InvoicePdfService pdfService) {
    this.invoiceService = invoiceService;
    this.customerService = customerService;
    this.productService = productService;
    this.pdfService = pdfService;

    setSizeFull();
    setPadding(false);
    setSpacing(false);
  }

  public static void navigateTo(Long invoiceId) {
    if (invoiceId != null) {
      UI.getCurrent().navigate(InvoiceView.class, new RouteParameters("invoiceId", invoiceId.toString()));
    } else {
      UI.getCurrent().navigate(InvoiceView.class);
    }
  }

  // ── UI Construction ───────────────────────────────────────────────

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    allCustomers = customerService.findAll();
    allProducts = productService.findAll();

    currentInvoice = event.getRouteParameters()
        .getLong("invoiceId")
        .flatMap(invoiceService::findById)
        .orElseGet(() -> Invoice.builder()
            .invoiceDate(LocalDate.now())
            .subtotal(BigDecimal.ZERO)
            .totalTax(BigDecimal.ZERO)
            .totalAmount(BigDecimal.ZERO)
            .build());

    // Populate in-memory line items from the persisted invoice
    lineItemRows.clear();
    currentInvoice.getItems()
        .stream()
        .map(item -> LineItemRow.fromLineItem(item, allProducts))
        .forEach(lineItemRows::add);

    buildUI();
    applyReadOnlyState();
  }

  private void buildUI() {
    removeAll();

    var content = new VerticalLayout();
    content.setWidthFull();
    content.setPadding(true);
    content.setSpacing(true);
    content.setMaxWidth("1400px");

    content.add(
        buildActionBar(),
        buildHeaderSection(),
        buildTabSheetSection(),
        buildFooterSection()
    );

    var scroller = new Scroller(content);
    scroller.setSizeFull();
    scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
    add(scroller);
    setFlexGrow(1, scroller);
  }

  // ── Action bar ─────────────────────────────────────────────────────
  private HorizontalLayout buildActionBar() {
    var breadcrumb = new Span();
    breadcrumb.getStyle().set("color", "var(--vaadin-text-color-secondary)")
        .set("font-size", "var(--aura-font-size-s)");
    var backLink = new Anchor("invoices", "Invoices");
    backLink.getStyle().set("color", "var(--vaadin-color-primary)");
    breadcrumb.add(backLink, new Span(" › "), new Span(
        currentInvoice.getInvoiceNumber() != null ? currentInvoice.getInvoiceNumber() : "New"
    ));

    statusBadge = InvoiceUtils.buildStatusBadge(currentInvoice.getStatus());

    saveDraftBtn = new Button("Save Draft", VaadinIcon.CLOUD.create(), _ -> saveDraft());
    saveDraftBtn.setId("save-draft-btn");

    finalizeBtn = new Button("Finalize", VaadinIcon.CHECK_CIRCLE.create(), _ -> finalizeInvoice());
    finalizeBtn.addThemeVariants(ButtonVariant.PRIMARY);
    finalizeBtn.setId("finalize-btn");

    revertBtn = new Button("Revert to Draft", VaadinIcon.EDIT.create(), _ -> revertToDraft());
    revertBtn.addThemeVariants(ButtonVariant.TERTIARY);
    revertBtn.setId("revert-to-draft-btn");

    var previewBtn = new Button("Preview", VaadinIcon.EYE.create(), _ -> openPreview());
    previewBtn.setId("preview-btn");

    var right = new HorizontalLayout(statusBadge, saveDraftBtn, finalizeBtn, revertBtn, previewBtn);
    right.setAlignItems(FlexComponent.Alignment.CENTER);
    right.setSpacing(true);

    var bar = new HorizontalLayout(breadcrumb, right);
    bar.setWidthFull();
    bar.setAlignItems(FlexComponent.Alignment.CENTER);
    bar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    bar.getStyle()
        .set("background", "var(--vaadin-background-color)")
        .set("border-radius", "var(--vaadin-radius-l)")
        .set("padding", "var(--vaadin-padding-m)")
        .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)");
    return bar;
  }

  // ── Header section ─────────────────────────────────────────────────
  private HorizontalLayout buildHeaderSection() {
    // LEFT — customer
    customerCombo = new ComboBox<>("Customer");
    customerCombo.setItems(allCustomers);
    customerCombo.setItemLabelGenerator(c -> c.getName() + " (" + c.getGstin() + ")");
    customerCombo.setWidthFull();
    customerCombo.setId("customer-combo");

    customerAddressBlock = new Div();
    customerAddressBlock.getStyle()
        .set("color", "var(--vaadin-text-color-secondary)")
        .set("font-size", "var(--aura-font-size-s)")
        .set("white-space", "pre-line")
        .set("min-height", "60px");

    // Pre-fill if editing
    if (currentInvoice.getBilledTo() != null) {
      allCustomers.stream()
          .filter(c -> c.getId().equals(currentInvoice.getBilledTo()))
          .findFirst()
          .ifPresent(c -> {
            customerCombo.setValue(c);
            updateCustomerAddress(c);
          });
    }

    customerCombo.addValueChangeListener(e -> {
      if (e.getValue() != null) updateCustomerAddress(e.getValue());
      else customerAddressBlock.setText("");
    });

    var leftLayout = new VerticalLayout(customerCombo, customerAddressBlock);
    leftLayout.setPadding(false);
    leftLayout.setSpacing(false);
    leftLayout.setWidth("50%");

    // RIGHT — invoice meta
    invoiceNumberField = new TextField("Invoice Number");
    invoiceNumberField.setId("invoice-number-field");
    invoiceNumberField.setValue(currentInvoice.getInvoiceNumber() != null ? currentInvoice.getInvoiceNumber() : "");

    invoiceDatePicker = new DatePicker("Invoice Date");
    invoiceDatePicker.setId("invoice-date-picker");
    invoiceDatePicker.setValue(currentInvoice.getInvoiceDate() != null ? currentInvoice.getInvoiceDate() : LocalDate.now());

    dueDatePicker = new DatePicker("Due Date");
    dueDatePicker.setId("due-date-picker");
    if (currentInvoice.getDueDate() != null) dueDatePicker.setValue(currentInvoice.getDueDate());

    dueTermSelect = new Select<>();
    dueTermSelect.setLabel("Terms");
    dueTermSelect.setItems(DueTerm.values());
    dueTermSelect.setItemLabelGenerator(DueTerm::getLabel);
    dueTermSelect.setId("due-term-select");
    dueTermSelect.addValueChangeListener(e -> applyDueTerm(e.getValue()));

    var rightForm = new FormLayout(invoiceNumberField, invoiceDatePicker, dueDatePicker, dueTermSelect);
    rightForm.setWidth("50%");

    var header = new HorizontalLayout(leftLayout, rightForm);
    header.setWidthFull();
    header.setAlignItems(FlexComponent.Alignment.START);
    header.getStyle()
        .set("background", "var(--vaadin-background-color)")
        .set("border-radius", "var(--vaadin-radius-l)")
        .set("padding", "var(--vaadin-padding-m)")
        .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)");
    return header;
  }

  // ── Tab sheet ─────────────────────────────────────────────────────
  private Div buildTabSheetSection() {
    var tabSheet = new TabSheet();
    tabSheet.setWidthFull();

    tabSheet.add(new Tab("Invoice Lines"), buildInvoiceLinesTab());
    tabSheet.add(new Tab("Other Info"), buildOtherInfoTab());

    var wrapper = new Div(tabSheet);
    wrapper.setWidthFull();
    wrapper.getStyle()
        .set("background", "var(--vaadin-background-color)")
        .set("border-radius", "var(--vaadin-radius-l)")
        .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)");
    return wrapper;
  }

  // ── Invoice Lines tab ─────────────────────────────────────────────
  private VerticalLayout buildInvoiceLinesTab() {
    lineGrid = new Grid<>(LineItemRow.class, false);
    lineGrid.addThemeVariants(GridVariant.NO_BORDER);
    lineGrid.setAllRowsVisible(true);
    lineGrid.setWidthFull();
    lineGrid.setId("line-items-grid");

    // Product column — ComboBox editor in-cell via component column
    lineGrid.addComponentColumn(row -> {
      var combo = new ComboBox<Product>();
      combo.setItems(allProducts);
      combo.setItemLabelGenerator(Product::getName);
      combo.setValue(row.getProduct());
      combo.setWidthFull();
      combo.setId("product-combo-" + lineItemRows.indexOf(row));
      combo.addValueChangeListener(e -> {
        if (e.getValue() != null) {
          row.setProduct(e.getValue());
          row.setUnitPrice(e.getValue().getUnitPrice() != null ? e.getValue().getUnitPrice() : BigDecimal.ZERO);
          row.setGstRate(e.getValue().getGstRate() != null ? e.getValue().getGstRate() : BigDecimal.ZERO);
          row.recalculate();
          lineGrid.getDataProvider().refreshAll();
          recalculateTotals();
        }
      });
      return combo;
    }).setHeader("Product").setFlexGrow(3);

    // Quantity
    lineGrid.addComponentColumn(row -> {
      var qtyField = new NumberField();
      qtyField.setValue(row.getQuantity() != null ? row.getQuantity().doubleValue() : 1.0);
      qtyField.setMin(1);
      qtyField.setStepButtonsVisible(true);
      qtyField.setWidth("110px");
      qtyField.addValueChangeListener(e -> {
        if (e.getValue() != null) {
          row.setQuantity(e.getValue().intValue());
          row.recalculate();
          lineGrid.getDataProvider().refreshAll();
          recalculateTotals();
        }
      });
      return qtyField;
    }).setHeader("Quantity").setFlexGrow(0).setWidth("130px");

    // Price
    lineGrid.addComponentColumn(row -> {
      var priceField = new NumberField();
      priceField.setValue(row.getUnitPrice() != null ? row.getUnitPrice().doubleValue() : 0.0);
      priceField.setMin(0);
      priceField.setPrefixComponent(new Span("₹"));
      priceField.setWidth("130px");
      priceField.addValueChangeListener(e -> {
        if (e.getValue() != null) {
          row.setUnitPrice(BigDecimal.valueOf(e.getValue()));
          row.recalculate();
          lineGrid.getDataProvider().refreshAll();
          recalculateTotals();
        }
      });
      return priceField;
    }).setHeader("Price").setFlexGrow(0).setWidth("160px");

    // Tax chip — read-only
    lineGrid.addComponentColumn(row -> {
      if (row.getGstRate() == null || row.getGstRate().compareTo(BigDecimal.ZERO) == 0) {
        return new Span("—");
      }
      return InvoiceUtils.buildTaxBadge(row.getGstRate());
    }).setHeader("Taxes").setFlexGrow(0).setWidth("120px");

    // Tax excl. — computed, read-only
    lineGrid.addColumn(row -> {
      if (row.getTaxExclAmount() == null) return "₹ 0.00";
      return "₹ " + String.format("%,.2f", row.getTaxExclAmount());
    }).setHeader("Tax Excl.").setFlexGrow(1).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

    // Delete action
    lineGrid.addComponentColumn(row -> {
      var delBtn = new Button(new Icon(VaadinIcon.TRASH));
      delBtn.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.TERTIARY);
      delBtn.setAriaLabel("Delete line");
      delBtn.addClickListener(_ -> {
        lineItemRows.remove(row);
        lineGrid.setItems(lineItemRows);
        recalculateTotals();
      });
      return delBtn;
    }).setFlexGrow(0).setWidth("60px");

    lineGrid.setItems(lineItemRows);

    var addLineBtn = new Button("+ Add a line");
    addLineBtn.addThemeVariants(ButtonVariant.TERTIARY);
    addLineBtn.setId("add-line-btn");
    addLineBtn.addClickListener(_ -> {
      lineItemRows.add(new LineItemRow());
      lineGrid.setItems(lineItemRows);
    });

    var tab = new VerticalLayout(lineGrid, addLineBtn);
    tab.setPadding(false);
    tab.setSpacing(false);
    return tab;
  }

  // ── Other Info tab ────────────────────────────────────────────────
  private FormLayout buildOtherInfoTab() {
    transportSelect = new Select<>();
    transportSelect.setLabel("Transport");
    transportSelect.setItems(Invoice.Transport.values());
    transportSelect.setItemLabelGenerator(t -> switch (t) {
      case SELF -> "Self";
      case COURIER -> "Courier";
    });
    transportSelect.setId("transport-select");
    if (currentInvoice.getTransport() != null) transportSelect.setValue(currentInvoice.getTransport());

    courierNameField = new TextField("Courier Name");
    courierNameField.setId("courier-name-field");
    courierNameField.setValue(currentInvoice.getCourierName() != null ? currentInvoice.getCourierName() : "");

    vehicleNumberField = new TextField("Vehicle Number");
    vehicleNumberField.setId("vehicle-number-field");
    vehicleNumberField.setValue(currentInvoice.getVehicleNumber() != null ? currentInvoice.getVehicleNumber() : "");

    eWayBillField = new TextField("E-Way Bill Number");
    eWayBillField.setId("eway-bill-field");
    eWayBillField.setValue(currentInvoice.getEWayBillNumber() != null ? currentInvoice.getEWayBillNumber() : "");

    var form = new FormLayout(transportSelect, courierNameField, vehicleNumberField, eWayBillField);
    form.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1),
        new FormLayout.ResponsiveStep("600px", 2)
    );
    return form;
  }

  // ── Footer (Terms + Totals) ───────────────────────────────────────
  private HorizontalLayout buildFooterSection() {
    termsArea = new TextArea();
    termsArea.setPlaceholder("Terms and Conditions");
    termsArea.setId("terms-area");
    termsArea.setWidthFull();
    termsArea.setMinHeight("120px");
    if (currentInvoice.getTermsAndConditions() != null) {
      termsArea.setValue(currentInvoice.getTermsAndConditions());
    }

    var termsWrapper = new VerticalLayout(new H3("Terms & Conditions"), termsArea);
    termsWrapper.setPadding(true);
    termsWrapper.setWidth("50%");
    termsWrapper.getStyle()
        .set("background", "var(--vaadin-background-color)")
        .set("border-radius", "var(--vaadin-radius-l)")
        .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)");

    totalsSection = new VerticalLayout();
    totalsSection.setPadding(true);
    totalsSection.setWidth("50%");
    totalsSection.getStyle()
        .set("background", "var(--vaadin-background-color)")
        .set("border-radius", "var(--vaadin-radius-l)")
        .set("box-shadow", "0 1px 4px rgba(0,0,0,.12)");

    recalculateTotals();

    var footer = new HorizontalLayout(termsWrapper, totalsSection);
    footer.setWidthFull();
    footer.setAlignItems(FlexComponent.Alignment.STRETCH);
    footer.setSpacing(true);
    return footer;
  }

  // ── Totals recalculation ──────────────────────────────────────────
  private void recalculateTotals() {
    BigDecimal untaxed = BigDecimal.ZERO;
    Map<String, BigDecimal> slabs = new LinkedHashMap<>();

    for (LineItemRow row : lineItemRows) {
      if (row.getTaxExclAmount() != null) {
        untaxed = untaxed.add(row.getTaxExclAmount());
      }
      if (row.getGstRate() != null && row.getGstRate().compareTo(BigDecimal.ZERO) > 0 && row.getTaxAmount() != null) {
        String key = "GST " + row.getGstRate().stripTrailingZeros().toPlainString() + "%";
        slabs.merge(key, row.getTaxAmount(), BigDecimal::add);
      }
    }
    BigDecimal total = untaxed;
    for (BigDecimal v : slabs.values()) total = total.add(v);

    // Update invoice fields for persistence
    currentInvoice.setSubtotal(untaxed);
    currentInvoice.setTotalTax(total.subtract(untaxed));
    currentInvoice.setTotalAmount(total);

    totalsSection.removeAll();
    totalsSection.add(new H3("Totals"));

    totalsSection.add(buildTotalRow("Untaxed Amount", untaxed, false));
    for (Map.Entry<String, BigDecimal> entry : slabs.entrySet()) {
      totalsSection.add(buildTotalRow(entry.getKey(), entry.getValue(), false));
    }

    var divider = new Div();
    divider.getStyle()
        .set("border-top", "1px solid var(--vaadin-border-color-secondary)")
        .set("width", "100%")
        .set("margin", "var(--vaadin-padding-xs) 0");
    totalsSection.add(divider);
    totalsSection.add(buildTotalRow("Total", total, true));
  }

  private HorizontalLayout buildTotalRow(String label, BigDecimal value, boolean bold) {
    var lbl = new Span(label + ":");
    var val = new Span("₹ " + String.format("%,.2f", value));
    if (bold) {
      lbl.getStyle().set("font-weight", "bold");
      val.getStyle().set("font-weight", "bold")
          .set("color", "var(--vaadin-color-success-text)");
    } else {
      lbl.getStyle().set("color", "var(--vaadin-text-color-secondary)");
    }
    var row = new HorizontalLayout(lbl, val);
    row.setWidthFull();
    row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    row.setAlignItems(FlexComponent.Alignment.CENTER);
    return row;
  }

  // ── Due term helper ───────────────────────────────────────────────
  private void applyDueTerm(DueTerm term) {
    if (term == null) return;
    var base = invoiceDatePicker.getValue() != null ? invoiceDatePicker.getValue() : LocalDate.now();
    var due = switch (term) {
      case IMMEDIATE -> base;
      case DAYS_15 -> base.plusDays(15);
      case DAYS_30 -> base.plusDays(30);
      case DAYS_45 -> base.plusDays(45);
      case DAYS_60 -> base.plusDays(60);
      case END_OF_MONTH -> base.withDayOfMonth(base.lengthOfMonth());
    };
    dueDatePicker.setValue(due);
  }

  // ── Actions ───────────────────────────────────────────────────────
  private void saveDraft() {
    collectFormData();
    currentInvoice.setStatus(InvoiceStatus.DRAFT);
    currentInvoice = invoiceService.save(currentInvoice);
    showNotification("Draft saved.", NotificationVariant.SUCCESS);
    refreshActionBar();
  }

  private void finalizeInvoice() {
    collectFormData();
    currentInvoice.setStatus(InvoiceStatus.FINALIZED);
    currentInvoice = invoiceService.save(currentInvoice);
    showNotification("Invoice finalized.", NotificationVariant.SUCCESS);
    applyReadOnlyState();
    refreshActionBar();
  }

  private void revertToDraft() {
    currentInvoice.setStatus(InvoiceStatus.DRAFT);
    currentInvoice = invoiceService.save(currentInvoice);
    showNotification("Invoice reverted to draft — you can now edit it.", NotificationVariant.SUCCESS);
    applyReadOnlyState();
    refreshActionBar();
  }

  private void openPreview() {
    collectFormData();
    Customer customer = customerCombo.getValue();
    var dialog = new InvoicePreviewDialog(currentInvoice, customer, allProducts, pdfService);
    dialog.open();
  }

  // ── Form helpers ──────────────────────────────────────────────────
  private void collectFormData() {
    currentInvoice.setInvoiceNumber(invoiceNumberField.getValue());
    currentInvoice.setInvoiceDate(invoiceDatePicker.getValue());
    currentInvoice.setDueDate(dueDatePicker.getValue());

    Customer sel = customerCombo.getValue();
    currentInvoice.setBilledTo(sel != null ? sel.getId() : null);

    currentInvoice.setTransport(transportSelect.getValue());
    currentInvoice.setCourierName(courierNameField.getValue());
    currentInvoice.setVehicleNumber(vehicleNumberField.getValue());
    currentInvoice.setEWayBillNumber(eWayBillField.getValue());
    currentInvoice.setTermsAndConditions(termsArea.getValue());

    // Rebuild line items list from in-memory rows
    // The order of insertion into this list determines the `line_order` in the database.
    var items = new ArrayList<LineItem>();
    for (LineItemRow row : lineItemRows) {
      if (row.getProduct() == null) continue;
      items.add(LineItem.builder()
          .id(row.getLineItemId())
          .productId(row.getProduct().getId())
          .quantity(row.getQuantity() != null ? row.getQuantity() : 1)
          .unitPrice(row.getUnitPrice() != null ? row.getUnitPrice() : BigDecimal.ZERO)
          .gstRate(row.getGstRate() != null ? row.getGstRate() : BigDecimal.ZERO)
          .taxAmount(row.getTaxAmount() != null ? row.getTaxAmount() : BigDecimal.ZERO)
          .totalAmount(row.getTotalAmount() != null ? row.getTotalAmount() : BigDecimal.ZERO)
          .build());
    }
    currentInvoice.setItems(items);
  }

  private void applyReadOnlyState() {
    boolean isFinalized = currentInvoice != null && currentInvoice.getStatus() == InvoiceStatus.FINALIZED;
    if (customerCombo != null) customerCombo.setReadOnly(isFinalized);
    if (invoiceNumberField != null) invoiceNumberField.setReadOnly(isFinalized);
    if (invoiceDatePicker != null) invoiceDatePicker.setReadOnly(isFinalized);
    if (dueDatePicker != null) dueDatePicker.setReadOnly(isFinalized);
    if (dueTermSelect != null) dueTermSelect.setEnabled(!isFinalized);
    if (transportSelect != null) transportSelect.setEnabled(!isFinalized);
    if (courierNameField != null) courierNameField.setReadOnly(isFinalized);
    if (vehicleNumberField != null) vehicleNumberField.setReadOnly(isFinalized);
    if (eWayBillField != null) eWayBillField.setReadOnly(isFinalized);
    if (termsArea != null) termsArea.setReadOnly(isFinalized);
    if (lineGrid != null) lineGrid.setEnabled(!isFinalized);
    if (saveDraftBtn != null) saveDraftBtn.setVisible(!isFinalized);
    if (finalizeBtn != null) finalizeBtn.setVisible(!isFinalized);
    if (revertBtn != null) revertBtn.setVisible(isFinalized);
  }

  private void refreshActionBar() {
    statusBadge.setText(currentInvoice.getStatus().name());
    statusBadge.getElement().getThemeList().clear();
    if (currentInvoice.getStatus() == InvoiceStatus.FINALIZED) {
      statusBadge.addThemeVariants(BadgeVariant.SUCCESS);
    } else {
      statusBadge.addThemeVariants(BadgeVariant.WARNING);
    }
  }

  private void updateCustomerAddress(Customer c) {
    customerAddressBlock.setText(
        c.getStreet() + "\n" + c.getCity() + ", " + c.getState() + " – " + c.getPostalCode() +
            "\nGSTIN: " + c.getGstin()
    );
  }

  // ── Inner types ───────────────────────────────────────────────────

  @Getter
  enum DueTerm {
    IMMEDIATE("Immediate"),
    DAYS_15("15 Days"),
    DAYS_30("30 Days"),
    DAYS_45("45 Days"),
    DAYS_60("60 Days"),
    END_OF_MONTH("End of Month");

    private final String label;

    DueTerm(String label) {
      this.label = label;
    }
  }

  /// In-memory mutable DTO for one line of the invoice (not an entity).
  @Getter
  static class LineItemRow {
    private Long lineItemId;
    @Setter
    private Product product;
    @Setter
    private Integer quantity = 1;
    @Setter
    private BigDecimal unitPrice = BigDecimal.ZERO;
    @Setter
    private BigDecimal gstRate = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal taxExclAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public LineItemRow() {
    }

    public static LineItemRow fromLineItem(LineItem item, List<Product> products) {
      var row = new LineItemRow();
      row.lineItemId = item.getId();
      row.quantity = item.getQuantity() != null ? item.getQuantity() : 1;
      row.unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
      row.gstRate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
      if (item.getProductId() != null) {
        products.stream()
            .filter(p -> p.getId().equals(item.getProductId()))
            .findFirst()
            .ifPresent(p -> row.product = p);
      }
      row.recalculate();
      return row;
    }

    public void recalculate() {
      BigDecimal qty = BigDecimal.valueOf(quantity != null ? quantity : 1);
      BigDecimal price = unitPrice != null ? unitPrice : BigDecimal.ZERO;
      BigDecimal rate = gstRate != null ? gstRate : BigDecimal.ZERO;
      taxExclAmount = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
      taxAmount = taxExclAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      totalAmount = taxExclAmount.add(taxAmount);
    }
  }
}
