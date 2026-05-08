package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceService;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceStatus;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductService;
import com.rkscientificindustries.invoice.ui.MainLayout;
import com.rkscientificindustries.invoice.ui.utils.AppConstants;
import com.rkscientificindustries.invoice.ui.utils.InvoiceUtils;
import com.vaadin.componentfactory.Breadcrumb;
import com.vaadin.componentfactory.Breadcrumbs;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import jakarta.annotation.security.PermitAll;
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
@StyleSheet("invoice-view.css")
@Route(value = "invoices/details/:invoiceId?", layout = MainLayout.class)
@PermitAll
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
  private ComboBox<Customer> billedCustomerCombo;
  private ComboBox<Customer> shippedCustomerCombo;
  private Div billedCustomerAddressBlock;
  private Div shippedCustomerAddressBlock;
  private DatePicker invoiceDatePicker;
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
  private Button previewBtn;

  public InvoiceView(InvoiceService invoiceService, CustomerService customerService,
                     ProductService productService, InvoicePdfService pdfService) {
    this.invoiceService = invoiceService;
    this.customerService = customerService;
    this.productService = productService;
    this.pdfService = pdfService;

    addClassName("invoice-view");
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
    var breadcrumbs = new Breadcrumbs();
    breadcrumbs.addClassName("styled-breadcrumb");
    breadcrumbs.add(
        new Breadcrumb("Invoices", "invoices"),
        new Breadcrumb(currentInvoice.getInvoiceNumber() != null ? String.valueOf(currentInvoice.getInvoiceNumber()) : "New")
    );

    statusBadge = InvoiceUtils.buildStatusBadge(currentInvoice.getStatus());

    saveDraftBtn = new Button("Save Draft", VaadinIcon.CLOUD.create(), _ -> saveDraft());
    saveDraftBtn.addClassName("invoice-action-button");
    saveDraftBtn.setId("save-draft-btn");

    finalizeBtn = new Button("Finalize", VaadinIcon.CHECK_CIRCLE.create(), _ -> finalizeInvoice());
    finalizeBtn.addThemeVariants(ButtonVariant.PRIMARY);
    finalizeBtn.addClassName("invoice-action-button");
    finalizeBtn.setId("finalize-btn");

    revertBtn = new Button("Revert to Draft", VaadinIcon.EDIT.create(), _ -> revertToDraft());
    revertBtn.addThemeVariants(ButtonVariant.TERTIARY);
    revertBtn.addClassName("invoice-action-button");
    revertBtn.setId("revert-to-draft-btn");

    previewBtn = new Button("Preview", VaadinIcon.EYE.create(), _ -> openPreview());
    previewBtn.addClassName("invoice-action-button");
    previewBtn.setId("preview-btn");

    var right = new HorizontalLayout(statusBadge, saveDraftBtn, finalizeBtn, revertBtn, previewBtn);
    right.setAlignItems(FlexComponent.Alignment.CENTER);
    right.setSpacing(true);
    right.addClassName("invoice-action-bar-actions");

    var bar = new HorizontalLayout(breadcrumbs, right);
    bar.setWidthFull();
    bar.setAlignItems(FlexComponent.Alignment.CENTER);
    bar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    bar.addClassName("invoice-action-bar");
    bar.addClassNames("invoice-card", "invoice-card-padded");
    return bar;
  }

  // ── Header section ─────────────────────────────────────────────────
  private HorizontalLayout buildHeaderSection() {
    billedCustomerCombo = new ComboBox<>("Billed To");
    billedCustomerCombo.setItems(allCustomers);
    billedCustomerCombo.setItemLabelGenerator(c -> c.getName() + " (" + c.getGstin() + ")");
    billedCustomerCombo.setWidthFull();
    billedCustomerCombo.setRequiredIndicatorVisible(true);
    billedCustomerCombo.setId("billed-customer-combo");

    shippedCustomerCombo = new ComboBox<>("Shipped To");
    shippedCustomerCombo.setItems(allCustomers);
    shippedCustomerCombo.setItemLabelGenerator(c -> c.getName() + " (" + c.getGstin() + ")");
    shippedCustomerCombo.setWidthFull();
    shippedCustomerCombo.setRequiredIndicatorVisible(true);
    shippedCustomerCombo.setId("shipped-customer-combo");

    billedCustomerAddressBlock = new Div();
    billedCustomerAddressBlock.addClassName("customer-address-block");

    shippedCustomerAddressBlock = new Div();
    shippedCustomerAddressBlock.addClassName("customer-address-block");

    billedCustomerCombo.addValueChangeListener(e -> {
      updateCustomerAddress(billedCustomerAddressBlock, e.getValue());
      if (e.getValue() != null && (shippedCustomerCombo.getValue() == null || shippedCustomerCombo.getValue()
          .equals(e.getOldValue()))) {
        shippedCustomerCombo.setValue(e.getValue());
      }
      updateActionBarVisibility();
    });

    shippedCustomerCombo.addValueChangeListener(e -> {
      if (e.getValue() == null && billedCustomerCombo.getValue() != null) {
        shippedCustomerCombo.setValue(billedCustomerCombo.getValue());
        return;
      }
      updateCustomerAddress(shippedCustomerAddressBlock, e.getValue());
    });

    var billedPanel = new VerticalLayout(billedCustomerCombo, billedCustomerAddressBlock);
    billedPanel.setPadding(false);
    billedPanel.setSpacing(false);

    var shippedPanel = new VerticalLayout(shippedCustomerCombo, shippedCustomerAddressBlock);
    shippedPanel.setPadding(false);
    shippedPanel.setSpacing(false);

    var billedCustomer = findCustomerById(currentInvoice.getBilledTo());
    if (billedCustomer != null) {
      billedCustomerCombo.setValue(billedCustomer);
    }

    var shippedCustomer = findCustomerById(currentInvoice.getShippedTo());
    if (shippedCustomer != null) {
      shippedCustomerCombo.setValue(shippedCustomer);
    } else if (billedCustomer != null) {
      shippedCustomerCombo.setValue(billedCustomer);
    }

    updateCustomerAddress(billedCustomerAddressBlock, billedCustomerCombo.getValue());
    updateCustomerAddress(shippedCustomerAddressBlock, shippedCustomerCombo.getValue());

    invoiceDatePicker = new DatePicker("Invoice Date");
    invoiceDatePicker.setId("invoice-date-picker");
    invoiceDatePicker.setValue(currentInvoice.getInvoiceDate() != null ? currentInvoice.getInvoiceDate() : LocalDate.now());
    invoiceDatePicker.setWidthFull();

    var dateSection = new VerticalLayout(invoiceDatePicker);
    dateSection.setPadding(false);
    dateSection.setSpacing(false);

    var header = new HorizontalLayout(billedPanel, shippedPanel, dateSection);
    header.setWidthFull();
    header.addClassNames("invoice-card", "invoice-card-padded", "flex-column");
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
    wrapper.addClassName("invoice-card");
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
    var productComboColumn = lineGrid.addComponentColumn(row -> {
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
          updateActionBarVisibility();
        }
      });
      return combo;
    });
    productComboColumn
        .setHeader("Product")
        .setFlexGrow(3)
        .setWidth("12rem");

    // Quantity
    var quantityColumn = lineGrid.addComponentColumn(row -> {
      var qtyField = new NumberField();
      qtyField.setValue(row.getQuantity() != null ? row.getQuantity().doubleValue() : 1.0);
      qtyField.setMin(1);
      qtyField.setStepButtonsVisible(true);
      qtyField.setWidth("7rem");
      qtyField.addValueChangeListener(e -> {
        if (e.getValue() != null) {
          row.setQuantity(e.getValue().intValue());
          row.recalculate();
          lineGrid.getDataProvider().refreshAll();
          recalculateTotals();
        }
      });
      return qtyField;
    });
    quantityColumn
        .setHeader("Quantity")
        .setFlexGrow(0)
        .setWidth("8rem");

    // Price
    var priceColumn = lineGrid.addComponentColumn(row -> {
      var priceField = new NumberField();
      priceField.setValue(row.getUnitPrice() != null ? row.getUnitPrice().doubleValue() : 0.0);
      priceField.setMin(0);
      priceField.setPrefixComponent(new Span("₹"));
      priceField.setWidth("7rem");
      priceField.addValueChangeListener(e -> {
        if (e.getValue() != null) {
          row.setUnitPrice(BigDecimal.valueOf(e.getValue()));
          row.recalculate();
          lineGrid.getDataProvider().refreshAll();
          recalculateTotals();
        }
      });
      return priceField;
    });
    priceColumn
        .setHeader("Price")
        .setFlexGrow(0)
        .setWidth("8rem");

    // Tax chip — read-only
    lineGrid.addComponentColumn(row -> {
      if (row.getGstRate() == null || row.getGstRate().compareTo(BigDecimal.ZERO) == 0) {
        return new Span("—");
      }
      return InvoiceUtils.buildTaxBadge(row.getGstRate());
    }).setHeader("Taxes")
        .setFlexGrow(0)
        .setWidth("7rem");

    // Tax excl. — computed, read-only
    lineGrid.addColumn(row -> {
      if (row.getTaxExclAmount() == null) return "₹ 0.00";
      return "₹ " + String.format("%,.2f", row.getTaxExclAmount());
    }).setHeader("Tax Excl.")
        .setFlexGrow(1)
        .setTextAlign(ColumnTextAlign.END);

    // Delete action
    var deleteColumn = lineGrid.addComponentColumn(row -> {
      var delBtn = new Button(new Icon(VaadinIcon.TRASH));
      delBtn.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.TERTIARY);
      delBtn.setAriaLabel("Delete line");
      delBtn.addClickListener(_ -> {
        lineItemRows.remove(row);
        lineGrid.setItems(lineItemRows);
        recalculateTotals();
        updateActionBarVisibility();
      });
      return delBtn;
    });
    deleteColumn
        .setFlexGrow(0)
        .setWidth("4rem")
        .setTextAlign(ColumnTextAlign.CENTER);

    lineGrid.setItems(lineItemRows);

    var addLineBtn = new Button("+ Add a line");
    addLineBtn.addThemeVariants(ButtonVariant.TERTIARY);
    addLineBtn.setId("add-line-btn");
    addLineBtn.addClickListener(_ -> {
      lineItemRows.add(new LineItemRow());
      lineGrid.setItems(lineItemRows);
      updateActionBarVisibility();
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
    transportSelect.setItemLabelGenerator(Invoice.Transport::displayName);
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
    termsArea.setValue(AppConstants.DEFAULT_TERMS);

    var termsWrapper = new VerticalLayout(new H3("Terms & Conditions"), termsArea);
    termsWrapper.setPadding(true);
    termsWrapper.addClassNames("invoice-half-width", "invoice-card");

    totalsSection = new VerticalLayout();
    totalsSection.setPadding(true);
    totalsSection.addClassNames("invoice-half-width", "invoice-card");

    recalculateTotals();

    var footer = new HorizontalLayout(termsWrapper, totalsSection);
    footer.setWidthFull();
    footer.setAlignItems(FlexComponent.Alignment.STRETCH);
    footer.setSpacing(true);
    footer.addClassName("flex-column-reverse");
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
    divider.addClassName("totals-divider");
    totalsSection.add(divider);
    totalsSection.add(buildTotalRow("Total", total, true));
  }

  private HorizontalLayout buildTotalRow(String label, BigDecimal value, boolean bold) {
    var lbl = new Span(label + ":");
    lbl.addClassName("total-row-label");
    var val = new Span("₹ " + String.format("%,.2f", value));
    val.addClassName("total-row-value");

    var row = new HorizontalLayout(lbl, val);
    row.addClassName("total-row");
    if (bold) {
      row.addClassName("total-row-bold");
    }

    row.setWidthFull();
    row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    row.setAlignItems(FlexComponent.Alignment.CENTER);
    return row;
  }

  // ── Actions ───────────────────────────────────────────────────────
  private void saveDraft() {
    collectFormData();
    currentInvoice.setStatus(InvoiceStatus.DRAFT);
    currentInvoice = invoiceService.save(currentInvoice);
    showNotification("Draft saved.", NotificationVariant.SUCCESS);
    updateActionBarVisibility();
  }

  private void finalizeInvoice() {
    collectFormData();
    currentInvoice.setStatus(InvoiceStatus.FINALIZED);
    currentInvoice = invoiceService.save(currentInvoice);
    showNotification("Invoice finalized.", NotificationVariant.SUCCESS);
    applyReadOnlyState();
    updateActionBarVisibility();
  }

  private void revertToDraft() {
    currentInvoice.setStatus(InvoiceStatus.DRAFT);
    currentInvoice = invoiceService.save(currentInvoice);
    showNotification("Invoice reverted to draft — you can now edit it.", NotificationVariant.SUCCESS);
    applyReadOnlyState();
    updateActionBarVisibility();
  }

  private void openPreview() {
    collectFormData();
    Customer billedCustomer = billedCustomerCombo.getValue();
    Customer shippedCustomer = shippedCustomerCombo.getValue();
    var dialog = new InvoicePreviewDialog(currentInvoice, billedCustomer, shippedCustomer, allProducts, pdfService, termsArea.getValue());
    dialog.open();
  }

  // ── Form helpers ──────────────────────────────────────────────────
  private void collectFormData() {
    currentInvoice.setInvoiceDate(invoiceDatePicker.getValue());

    Customer billedCustomer = billedCustomerCombo.getValue();
    Customer shippedCustomer = shippedCustomerCombo.getValue();
    currentInvoice.setBilledTo(billedCustomer != null ? billedCustomer.getId() : null);
    currentInvoice.setShippedTo(shippedCustomer != null ? shippedCustomer.getId() : null);

    currentInvoice.setTransport(transportSelect.getValue());
    currentInvoice.setCourierName(courierNameField.getValue());
    currentInvoice.setVehicleNumber(vehicleNumberField.getValue());
    currentInvoice.setEWayBillNumber(eWayBillField.getValue());

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
    if (billedCustomerCombo != null) billedCustomerCombo.setReadOnly(isFinalized);
    if (shippedCustomerCombo != null) shippedCustomerCombo.setReadOnly(isFinalized);
    if (invoiceDatePicker != null) invoiceDatePicker.setReadOnly(isFinalized);
    if (transportSelect != null) transportSelect.setEnabled(!isFinalized);
    if (courierNameField != null) courierNameField.setReadOnly(isFinalized);
    if (vehicleNumberField != null) vehicleNumberField.setReadOnly(isFinalized);
    if (eWayBillField != null) eWayBillField.setReadOnly(isFinalized);
    if (termsArea != null) termsArea.setReadOnly(isFinalized);
    if (lineGrid != null) lineGrid.setEnabled(!isFinalized);
    
    updateActionBarVisibility();
  }

  private void updateActionBarVisibility() {
    if (currentInvoice.getStatus() != null) {
      statusBadge.setVisible(true);
      statusBadge.setText(currentInvoice.getStatus().name());
      statusBadge.getElement().getThemeList().clear();
      if (currentInvoice.getStatus() == InvoiceStatus.FINALIZED) {
        statusBadge.addThemeVariants(BadgeVariant.SUCCESS);
      } else {
        statusBadge.addThemeVariants(BadgeVariant.WARNING);
      }
    } else {
      statusBadge.setVisible(false);
    }

    boolean isNewInvoice = currentInvoice.getId() == null;
    boolean hasBilledCustomer = billedCustomerCombo != null && billedCustomerCombo.getValue() != null;
    boolean hasProducts = lineItemRows != null && lineItemRows.stream().anyMatch(row -> row.getProduct() != null);
    boolean isDraft = currentInvoice.getStatus() == null || currentInvoice.getStatus() == InvoiceStatus.DRAFT;

    if (saveDraftBtn != null) saveDraftBtn.setVisible(hasBilledCustomer && isDraft);
    if (finalizeBtn != null) finalizeBtn.setVisible(!isNewInvoice && hasProducts && isDraft);
    if (previewBtn != null) previewBtn.setVisible(!isNewInvoice && hasProducts);
    if (revertBtn != null) revertBtn.setVisible(currentInvoice.getStatus() == InvoiceStatus.FINALIZED);
  }

  private Customer findCustomerById(Long customerId) {
    if (customerId == null) {
      return null;
    }
    return allCustomers.stream()
        .filter(c -> c.getId().equals(customerId))
        .findFirst()
        .orElse(null);
  }

  private void updateCustomerAddress(Div addressBlock, Customer customer) {
    if (addressBlock == null) {
      return;
    }
    if (customer == null) {
      addressBlock.setText("");
      return;
    }

    addressBlock.setText(
        customer.getStreet() + "\n" + customer.getCity() + ", " + customer.getState() + " – " + customer.getPostalCode() +
            "\nGSTIN: " + customer.getGstin()
    );
  }

  /// In-memory mutable DTO for one line of the invoice (not an entity).
  @Getter
  static class LineItemRow {
    private Long lineItemId;
    @Setter private Product product;
    @Setter private Integer quantity = 1;
    @Setter private BigDecimal unitPrice = BigDecimal.ZERO;
    @Setter private BigDecimal gstRate = BigDecimal.ZERO;
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
