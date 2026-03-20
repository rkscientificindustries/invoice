package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductService;
import com.rkscientificindustries.invoice.backend.utils.State;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.rkscientificindustries.invoice.ui.utils.InvoiceUtils.showNotification;

@SpringComponent
@UIScope
public class InvoiceEditor extends VerticalLayout {
  private final ProductService productService;
  private final CustomerService customerService;
  // Form Fields
  private final TextField invoiceNumber = new TextField("Invoice Number");
  private final DatePicker invoiceDate = new DatePicker("Date");
  private final ComboBox<Customer> billedToField = new ComboBox<>("Billed To");
  private final ComboBox<Customer> shippedToField = new ComboBox<>("Shipped To");
  private final ComboBox<State> place = new ComboBox<>("Place of Supply");
  private final ComboBox<Invoice.Transport> transport = new ComboBox<>("Transport Mode");
  private final TextField courierName = new TextField("Courier Name");
  private final TextField vehicleNumber = new TextField("Vehicle Number");
  private final TextField eWayBillNumber = new TextField("E-Way Bill Number");
  private final IntegerField packageCount = new IntegerField("No. of Packages");
  // Totals
  private final Span subtotalLabel = new Span("Subtotal: 0.00");
  private final Span taxLabel = new Span("Tax: 0.00");
  private final Span totalLabel = new Span("Total: 0.00");

  private final Binder<Invoice> binder = new Binder<>(Invoice.class);
  private final Grid<LineItem> lineGrid = new Grid<>(LineItem.class, false);
  private final LineItemDialog lineItemDialog;
  private Invoice currentInvoice;
  private Button saveBtn;
  private List<Customer> customers = new ArrayList<>();

  private @Setter Consumer<Invoice> onSave;
  private @Setter Runnable onCancel;

  public InvoiceEditor(ProductService productService, CustomerService customerService) {
    this.productService = productService;
    this.customerService = customerService;
    this.lineItemDialog = new LineItemDialog(productService);

    setWidthFull();
    setPadding(true);
    setSpacing(true);

    createForm();
    prepareLineItems();
    createFooter();

    // Manual bindings
    binder.forField(invoiceNumber)
        .bind(Invoice::getInvoiceNumber, Invoice::setInvoiceNumber);

    binder.forField(invoiceDate)
        .bind(Invoice::getInvoiceDate, Invoice::setInvoiceDate);

    binder.forField(billedToField)
        .bind(
            invoice -> invoice.getBilledTo() != null ?
                customers.stream().filter(c -> c.getId().equals(invoice.getBilledTo())).findFirst().orElse(null) : null,
            (invoice, customer) -> invoice.setBilledTo(customer != null ? customer.getId() : null));

    binder.forField(shippedToField)
        .bind(
            invoice -> invoice.getShippedTo() != null ?
                customers.stream().filter(c -> c.getId().equals(invoice.getShippedTo())).findFirst()
                    .orElse(null) : null,
            (invoice, customer) -> invoice.setShippedTo(customer != null ? customer.getId() : null));

    binder.forField(place)
        .bind(Invoice::getPlace, Invoice::setPlace);

    binder.forField(transport)
        .bind(Invoice::getTransport, Invoice::setTransport);

    binder.forField(courierName)
        .bind(Invoice::getCourierName, Invoice::setCourierName);

    binder.forField(vehicleNumber)
        .bind(Invoice::getVehicleNumber, Invoice::setVehicleNumber);

    binder.forField(eWayBillNumber)
        .bind(Invoice::getEWayBillNumber, Invoice::setEWayBillNumber);

    binder.forField(packageCount)
        .bind(Invoice::getPackageCount, Invoice::setPackageCount);
  }

  private void createForm() {
    var formLayout = new FormLayout();

    customers = customerService.findAll();
    billedToField.setItems(customers);
    billedToField.setItemLabelGenerator(Customer::getName);

    shippedToField.setItems(customers);
    shippedToField.setItemLabelGenerator(Customer::getName);

    place.setItems(State.values());
    place.setItemLabelGenerator(state -> state.name().replace('_', ' '));

    transport.setItems(Invoice.Transport.values());
    transport.addValueChangeListener(e ->
        courierName.setVisible(e.getValue() == Invoice.Transport.COURIER));

    packageCount.setStepButtonsVisible(true);
    packageCount.setMin(1);

    formLayout.add(invoiceNumber, invoiceDate, billedToField, shippedToField, place, transport,
        courierName, vehicleNumber, eWayBillNumber, packageCount);
    add(formLayout);
  }

  private void prepareLineItems() {
    lineGrid.setWidthFull();
    lineGrid.addColumn(line -> productService.findById(line.getProductId())
            .map(Product::getName)
            .orElse("Unknown Product"))
        .setHeader("Item")
        .setFrozen(true)
        .setAutoWidth(true)
        .setFlexGrow(0);
    lineGrid.addColumn(LineItem::getUnitPrice).setHeader("Price");
    lineGrid.addColumn(LineItem::getQuantity).setHeader("Qty");
    lineGrid.addColumn(LineItem::getTotalAmount).setHeader("Total").setTextAlign(ColumnTextAlign.END);
    lineGrid.addComponentColumn(line -> {
          var actions = new HorizontalLayout();
          var editBtn = new Button(new Icon(VaadinIcon.EDIT));
          editBtn.addClickListener(_ -> openLineItemDialog(line, false));
          editBtn.setAriaLabel("Edit item");

          var deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
          deleteBtn.addClickListener(_ -> {
            currentInvoice.removeLineItem(line);
            reindexLineOrder();
            lineGrid.setItems(currentInvoice.getItems());
            calculateInvoiceTotals();
          });
          deleteBtn.addThemeVariants(ButtonVariant.ERROR);
          deleteBtn.setAriaLabel("Delete item");
          actions.add(editBtn, deleteBtn);
          return actions;
        })
        .setHeader("Actions")
        .setFrozenToEnd(true)
        .setAutoWidth(true)
        .setFlexGrow(0);

    var addLineBtn = new Button("Add Item", VaadinIcon.PLUS.create());
    addLineBtn.addClickListener(_ -> {
      var lineItem = LineItem.builder().build();
      if (currentInvoice.getItems() == null) {
        currentInvoice.setItems(new ArrayList<>());
      }
//      lineItem.setLineOrder(currentInvoice.getItems().size());
      openLineItemDialog(lineItem, true);
    });

    add(lineGrid, addLineBtn);
    lineGrid.setAllRowsVisible(true);
  }

  private void openLineItemDialog(LineItem lineItem, boolean isNew) {
    lineItemDialog.open(lineItem, isNew, savedLine -> {
      if (isNew) {
        if (currentInvoice.getItems() == null) currentInvoice.setItems(new ArrayList<>());
        currentInvoice.addLineItem(savedLine);
      }
      reindexLineOrder();
      lineGrid.setItems(currentInvoice.getItems());
      calculateInvoiceTotals();
    });
  }

  private void reindexLineOrder() {
    if (currentInvoice.getItems() == null) return;
    for (int i = 0; i < currentInvoice.getItems().size(); i++) {
//      var l = currentInvoice.getItems().get(i);
//      l.setLineOrder(i);
    }
  }

  private void createFooter() {
    var totalsLayout = new HorizontalLayout(subtotalLabel, taxLabel, totalLabel);
    totalsLayout.setSpacing(true);

    saveBtn = new Button("Save Invoice", _ -> save());
    saveBtn.setAriaLabel("Save Invoice");
    saveBtn.addThemeVariants(ButtonVariant.PRIMARY);

    var cancelBtn = new Button("Cancel", _ -> {
      if (onCancel != null) onCancel.run();
    });

    var actions = new HorizontalLayout(saveBtn, cancelBtn);

    add(totalsLayout, actions);
  }

  private void calculateInvoiceTotals() {
    if (currentInvoice.getItems() == null) return;

    var subtotal = currentInvoice.getItems().stream()
        .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    var tax = currentInvoice.getItems().stream()
        .map(LineItem::getTaxAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    var total = subtotal.add(tax);

    currentInvoice.setSubtotal(subtotal);
    currentInvoice.setTotalTax(tax);
    currentInvoice.setTotalAmount(total);

    subtotalLabel.setText("Subtotal: " + subtotal);
    taxLabel.setText("Tax: " + tax);
    totalLabel.setText("Total: " + total);
  }

  public void setInvoice(Invoice invoice) {
    this.currentInvoice = invoice;
    binder.readBean(invoice);
    lineGrid.setItems(invoice.getItems() != null ? invoice.getItems() : new ArrayList<>());
    calculateInvoiceTotals();
    updateButtonLabel();
  }

  private void updateButtonLabel() {
    if (isNewInvoice()) {
      saveBtn.setText("Save Invoice");
      saveBtn.setAriaLabel("Save Invoice");
    } else {
      saveBtn.setText("Update Invoice");
      saveBtn.setAriaLabel("Update Invoice");
    }
  }

  private boolean isNewInvoice() {
    return currentInvoice == null || currentInvoice.getId() == null;
  }

  private void save() {
    try {
      binder.writeBean(currentInvoice);
      if (onSave != null) {
        onSave.accept(currentInvoice);
      }
    } catch (ValidationException e) {
      showNotification("Please correct the errors in the form", NotificationVariant.ERROR);
    }
  }
}
