package com.rkscientificindustries.invoice.ui;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceService;
import com.rkscientificindustries.invoice.ui.invoices.InvoiceView;
import com.rkscientificindustries.invoice.ui.utils.InvoiceUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static com.rkscientificindustries.invoice.ui.utils.AppConstants.DATE_FMT;

@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class HomeView extends VerticalLayout {
  private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));

  public HomeView(InvoiceService invoiceService, CustomerService customerService) {
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    List<Invoice> recentInvoices = invoiceService.findRecentInvoices();

    add(createKpiRow(
        invoiceService.thisMonthInvoiceValue(),
        invoiceService.totalInvoiceValue(),
        invoiceService.averageInvoiceValue()
    ));
    add(createQuickActions());
    add(createRecentInvoicesSection(recentInvoices, customerService));
  }

  private HorizontalLayout createKpiRow(BigDecimal thisMonthValue, BigDecimal totalValue, BigDecimal averageValue) {
    var row = new HorizontalLayout(
        createKpiCard("This Month Invoice Value", formatCurrency(thisMonthValue), VaadinIcon.CALENDAR),
        createKpiCard("Total Invoice Value", formatCurrency(totalValue), VaadinIcon.COINS),
        createKpiCard("Average Invoice Value", formatCurrency(averageValue), VaadinIcon.CHART)
    );
    row.setWidthFull();
    row.setPadding(false);
    row.setSpacing(true);
    row.getStyle().set("flex-wrap", "wrap");
    return row;
  }

  private Card createKpiCard(String label, String value, VaadinIcon iconType) {
    var card = new Card();
    card.getStyle()
        .set("min-width", "220px")
        .set("flex", "1 1 220px");

    var content = new VerticalLayout();
    content.setPadding(true);
    content.setSpacing(false);
    content.setWidthFull();

    var titleRow = new HorizontalLayout(new Icon(iconType), new Span(label));
    titleRow.setWidthFull();
    titleRow.setSpacing(true);
    titleRow.setPadding(false);
    titleRow.setAlignItems(HorizontalLayout.Alignment.CENTER);

    var valueLabel = new H3(value);
    valueLabel.getStyle()
        .set("margin", "0")
        .set("font-size", "2.2rem")
        .set("line-height", "1.1");

    content.add(titleRow, valueLabel);
    card.add(content);
    return card;
  }

  private HorizontalLayout createQuickActions() {
    var newInvoice = createActionButton("New Invoice",
        VaadinIcon.PLUS,
        () -> InvoiceView.navigateTo(null)
    );
    newInvoice.addThemeVariants(ButtonVariant.PRIMARY);

    var customers = createActionButton("Customers",
        VaadinIcon.USER_CHECK,
        () -> getUI().ifPresent(ui -> ui.navigate("customers"))
    );
    var products = createActionButton("Products",
        VaadinIcon.PACKAGE,
        () -> getUI().ifPresent(ui -> ui.navigate("products"))
    );

    var actions = new HorizontalLayout(newInvoice, customers, products);
    actions.addClassName("quick-actions");
    actions.setWidthFull();
    actions.setPadding(false);
    actions.setSpacing(true);
    return actions;
  }

  private Button createActionButton(String label, VaadinIcon iconType, Runnable action) {
    var button = new Button();
    button.setIcon(new Icon(iconType));

    var text = new Span(label);
    text.addClassName("action-label");
    button.getElement().appendChild(text.getElement());
    button.getElement().setAttribute("aria-label", label);
    button.addClickListener(_ -> action.run());
    return button;
  }

  private VerticalLayout createRecentInvoicesSection(List<Invoice> recentInvoices, CustomerService customerService) {
    var section = new VerticalLayout();
    section.setWidthFull();
    section.setPadding(false);
    section.setSpacing(true);

    var title = new H3("Recent Invoices");
    title.getStyle().set("margin", "0");

    var viewAllBtn = new Button("View all invoices", _ -> getUI().ifPresent(ui -> ui.navigate("invoices")));
    viewAllBtn.addThemeVariants(ButtonVariant.TERTIARY);

    var header = new HorizontalLayout(title, viewAllBtn);
    header.setWidthFull();
    header.setPadding(false);
    header.setSpacing(true);
    header.setAlignItems(HorizontalLayout.Alignment.CENTER);
    header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);

    var grid = new Grid<>(Invoice.class, false);
    grid.addThemeVariants(GridVariant.ROW_STRIPES);
    grid.setWidthFull();
    grid.setAllRowsVisible(true);

    grid.addColumn(Invoice::getInvoiceNumber)
        .setHeader("Invoice #");

    grid.addColumn(invoice -> {
          if (invoice.getBilledTo() == null) return "—";
          return customerService.findById(invoice.getBilledTo())
              .map(Customer::getName)
              .orElse("Unknown");
        })
        .setHeader("Customer")
        .setAutoWidth(true);

    grid.addColumn(invoice -> invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().format(DATE_FMT) : "—")
        .setHeader("Invoice Date");

    grid.addColumn(invoice -> formatCurrency(invoice.getTotalAmount()))
        .setHeader("Total")
        .setTextAlign(ColumnTextAlign.END);

    var statusColumn = grid.addComponentColumn(invoice -> InvoiceUtils.buildStatusBadge(invoice.getStatus()))
        .setHeader("Status");
    configureStatusColumnForMobile(grid, statusColumn);

    grid.setItems(recentInvoices);
    grid.addItemClickListener(event -> {
      if (event.getItem().getId() != null)
        InvoiceView.navigateTo(event.getItem().getId());
    });

    section.add(header, grid);
    return section;
  }

  private void configureStatusColumnForMobile(Grid<Invoice> grid, Grid.Column<Invoice> statusColumn) {
    final int mobileBreakpoint = 756;

    grid.addAttachListener(_ -> grid.getUI().ifPresent(ui -> {
      ui.getPage()
          .executeJs("return window.innerWidth")
          .then(Integer.class, width -> statusColumn.setVisible(width > mobileBreakpoint));
      ui.getPage().addBrowserWindowResizeListener(event ->
          statusColumn.setVisible(event.getWidth() > mobileBreakpoint)
      );
    }));
  }

  private String formatCurrency(BigDecimal value) {
    return currencyFormat.format(value != null ? value : BigDecimal.ZERO);
  }
}
