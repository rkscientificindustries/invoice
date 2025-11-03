package com.rkscientificindustries.invoice.ui.customers;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
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

  public CustomerListView(CustomerService customerService) {
    this.customerService = customerService;
    this.detailsDialog = new CustomerDialog(customerService, this::refreshGrid);

    setSizeFull();
    add(createToolbar(), grid);
    configureGrid();
    loadCustomers();
  }

  private HorizontalLayout createToolbar() {
    var addCustomerBtn = new Button("Add Customer", e -> detailsDialog.openEmptyForm());
    addCustomerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    var toolbar = new HorizontalLayout(addCustomerBtn);
    toolbar.setWidthFull();
    toolbar.setJustifyContentMode(JustifyContentMode.END);
    return toolbar;
  }

  private void configureGrid() {
    configureColumns();
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
            .setAutoWidth(true);
  }

  private void loadCustomers() {
    grid.setItems(customerService.findAll());
  }

  private void refreshGrid(Customer customer) {
    grid.getListDataView().addItem(customer);
  }
}
