package com.rkscientificindustries.invoice.ui.customers;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("customers")
public class CustomerListView extends VerticalLayout {
  private final Grid<Customer> grid = new Grid<>(Customer.class, false);
  private final CustomerService customerService;
  private final CustomerDialog detailsDialog;
  private final HorizontalLayout contentLayout;
  private CustomerDetailPanel detailPanel;

  public CustomerListView(CustomerService customerService) {
    this.customerService = customerService;
    this.detailsDialog = new CustomerDialog(customerService, this::refreshGrid);

    setSizeFull();
    setPadding(false);
    setSpacing(false);
    getStyle()
        .set("overflow", "hidden")
        .set("height", "100vh");

    // Create content layout that will hold grid and detail panel
    contentLayout = new HorizontalLayout();
    contentLayout.setSizeFull();
    contentLayout.setPadding(false);
    contentLayout.setSpacing(false);

    // Grid container with its own scroll
    var gridContainer = new VerticalLayout();
    gridContainer.setSizeFull();
    gridContainer.setPadding(true);
    gridContainer.setSpacing(false);
    gridContainer.getStyle()
        .set("overflow", "auto")
        .set("position", "relative");

    grid.setHeight("auto");
    gridContainer.add(grid);

    contentLayout.add(gridContainer);
    contentLayout.setFlexGrow(1, gridContainer);

    add(createToolbar(), contentLayout);
    configureGrid();
    loadCustomers();
  }

  private HorizontalLayout createToolbar() {
    var addCustomerBtn = new Button("Add Customer", e -> detailsDialog.openEmptyForm());
    addCustomerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var toolbar = new HorizontalLayout(addCustomerBtn);
    toolbar.setWidthFull();
    toolbar.setJustifyContentMode(JustifyContentMode.END);
    toolbar.setPadding(true);
    toolbar.getStyle()
        .set("flex-shrink", "0")
        .set("background-color", "var(--lumo-base-color)")
        .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
        .set("z-index", "1");
    return toolbar;
  }

  private void configureGrid() {
    configureColumns();
    configureInteractions();
  }

  private void configureColumns() {
    grid.addColumn(Customer::getId)
            .setHeader("ID")
            .setFlexGrow(0)
            .setWidth("80px")
            .setSortable(true);

    grid.addColumn(Customer::getName)
            .setHeader("Name")
            .setAutoWidth(true)
            .setSortable(true);

    grid.addColumn(Customer::getGstin)
            .setHeader("GSTIN")
            .setAutoWidth(true)
            .setFlexGrow(1)
            .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
  }

  private void configureInteractions() {
    grid.addItemClickListener(event -> openCustomerDetailPanel(event.getItem()));

    grid.addCellFocusListener(event ->
            event.getItem().ifPresent(customer -> grid.asSingleSelect().setValue(customer)));

    Shortcuts.addShortcutListener(grid,
            e -> {
              var selected = grid.asSingleSelect().getValue();
              if (selected != null) {
                openCustomerDetailPanel(selected);
              }
            },
            Key.ENTER);
  }

  private void openCustomerDetailPanel(Customer customer) {
    if (customer == null) {
      return;
    }

    // Close existing panel if open
    if (detailPanel != null) {
      contentLayout.remove(detailPanel);
      detailPanel = null;
    }

    // Create new panel
    detailPanel = new CustomerDetailPanel(
            customerService,
            this::updateCustomerInGrid,
            this::handleCustomerDeleted,
            this::handlePanelClosed
    );

    // Add panel to content layout
    contentLayout.add(detailPanel);
    contentLayout.setFlexGrow(0, detailPanel);

    // Show customer details
    detailPanel.showCustomer(customer);
  }

  private void handlePanelClosed() {
    grid.asSingleSelect().clear();
    if (detailPanel != null) {
      contentLayout.remove(detailPanel);
      detailPanel = null;
    }
  }

  private void handleCustomerDeleted() {
    loadCustomers();
    if (detailPanel != null) {
      contentLayout.remove(detailPanel);
      detailPanel = null;
    }
  }

  private void updateCustomerInGrid(Customer customer) {
    grid.getListDataView().refreshItem(customer);
  }

  private void loadCustomers() {
    grid.setItems(customerService.findAll());
  }

  private void refreshGrid(Customer customer) {
    grid.getListDataView().addItem(customer);
  }
}
