package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceService;
import com.rkscientificindustries.invoice.ui.MainLayout;
import com.rkscientificindustries.invoice.ui.utils.InvoiceUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.rkscientificindustries.invoice.ui.utils.AppConstants.DATE_FMT;

@PageTitle("Invoices")
@Route(value = "invoices", layout = MainLayout.class)
public class InvoiceListView extends VerticalLayout {
  private final InvoiceService invoiceService;
  private final CustomerService customerService;

  private final Grid<Invoice> grid = new Grid<>(Invoice.class, false);

  public InvoiceListView(InvoiceService invoiceService, CustomerService customerService) {
    this.invoiceService = invoiceService;
    this.customerService = customerService;

    setSizeFull();
    setPadding(false);
    setSpacing(false);

    add(buildToolbar());
    add(buildGrid());
    setFlexGrow(1, grid);
  }

  private HorizontalLayout buildToolbar() {
    var newBtn = new Button("New Invoice", new Icon(VaadinIcon.PLUS), _ ->
        InvoiceView.navigateTo(null)
    );
    newBtn.addThemeVariants(ButtonVariant.PRIMARY);
    newBtn.setId("new-invoice-btn");

    var toolbar = new HorizontalLayout(newBtn);
    toolbar.setWidthFull();
    toolbar.setPadding(true);
    toolbar.setJustifyContentMode(JustifyContentMode.END);
    toolbar.getStyle().set("border-bottom", "1px solid var(--vaadin-border-color-secondary)");
    return toolbar;
  }

  private Grid<Invoice> buildGrid() {
    ListDataProvider<Invoice> dataProvider = new ListDataProvider<>(invoiceService.findAll());

    grid.addThemeVariants(GridVariant.ROW_STRIPES);
    grid.setSizeFull();

    grid.addColumn(Invoice::getInvoiceNumber)
        .setHeader("Invoice #")
        .setFlexGrow(0)
        .setAutoWidth(true)
        .setSortable(true);

    grid.addColumn(invoice -> {
      if (invoice.getBilledTo() == null) return "—";
      return customerService.findById(invoice.getBilledTo())
          .map(Customer::getName)
          .orElse("Unknown");
    }).setHeader("Customer").setAutoWidth(true);

    grid.addColumn(invoice -> invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FMT) : "—"
    ).setHeader("Invoice Date").setAutoWidth(true);

    grid.addColumn(Invoice::getSubtotal)
        .setHeader("Tax Excl.")
        .setTextAlign(ColumnTextAlign.END)
        .setAutoWidth(true);

    grid.addColumn(Invoice::getTotalAmount)
        .setHeader("Total")
        .setTextAlign(ColumnTextAlign.END)
        .setAutoWidth(true);

    grid.addComponentColumn(invoice -> InvoiceUtils.buildStatusBadge(invoice.getStatus()))
        .setHeader("Status")
        .setAutoWidth(true);

    grid.setDataProvider(dataProvider);

    grid.addItemClickListener(event ->
        InvoiceView.navigateTo(event.getItem().getId())
    );

    return grid;
  }
}
