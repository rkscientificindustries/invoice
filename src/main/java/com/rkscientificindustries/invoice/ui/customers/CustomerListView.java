package com.rkscientificindustries.invoice.ui.customers;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.ui.MainLayout;
import com.rkscientificindustries.invoice.ui.utils.FabButton;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Customers")
@Route(value = "customers", layout = MainLayout.class)
public class CustomerListView extends MasterDetailLayout {
  private final Grid<Customer> grid = new Grid<>(Customer.class, false);
  private final CustomerService customerService;
  private final CustomerDialog detailsDialog;
  private final ListDataProvider<Customer> dataProvider;

  public CustomerListView(CustomerService customerService) {
    this.customerService = customerService;
    this.detailsDialog = new CustomerDialog(customerService, this::addCustomer);

    setSizeFull();

    // Create master area with grid
    var masterContent = new VerticalLayout();
    masterContent.setSizeFull();
    masterContent.setPadding(false);
    masterContent.setSpacing(false);

    grid.setSizeFull();
    masterContent.add(grid);
    masterContent.setFlexGrow(1, grid);

    var addCustomerBtn = FabButton.create("Add Customer", e -> detailsDialog.openEmptyForm());
    masterContent.add(addCustomerBtn);

    setMaster(masterContent);

    // Initially hide detail
    setDetail(null);

    configureGrid();
    dataProvider = new ListDataProvider<>(customerService.findAll());
    grid.setDataProvider(dataProvider);

    // Configure master-detail behavior
    setMasterMinSize("400px");
    setDetailSize("500px");

    // Hide detail on backdrop click and escape key
    addBackdropClickListener(event -> closeDetail());
    addDetailEscapePressListener(event -> closeDetail());
  }

  private void configureGrid() {
    configureGridColumns();
    configureGridInteractions();
  }

  private void configureGridColumns() {
    grid.addComponentColumn(customer -> {
              int index = dataProvider.getItems().stream()
                      .toList()
                      .indexOf(customer) + 1;
              return new Span(String.valueOf(index));
            })
            .setHeader("#")
            .setFlexGrow(0)
            .setWidth("64px");

    grid.addColumn(Customer::getName)
            .setHeader("Name")
            .setAutoWidth(true);

    grid.addColumn(Customer::getGstin)
            .setHeader("GSTIN")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.END);
  }

  private void configureGridInteractions() {
    grid.addItemClickListener(clickEvent -> {
      grid.asSingleSelect().setValue(clickEvent.getItem());
      setDetail(createDetailContent(clickEvent.getItem()));
    });

    grid.addCellFocusListener(event ->
            event.getItem().ifPresent(customer -> grid.asSingleSelect().setValue(customer)));
    Shortcuts.addShortcutListener(grid,
            e -> {
              var selected = grid.asSingleSelect().getValue();
              setDetail(createDetailContent(selected));
            },
            Key.ENTER);
  }

  private VerticalLayout createDetailContent(Customer customer) {
    var content = new VerticalLayout();
    content.setSizeFull();
    content.setPadding(true);
    content.setSpacing(true);

    // Header with close button and actions
    var header = createDetailHeader(customer);
    content.add(header);

    // Customer information cards
    content.add(createBasicInfoCard(customer));
    content.add(createContactInfoCard(customer));
    content.add(createAddressCard(customer));

    return content;
  }

  private HorizontalLayout createDetailHeader(Customer customer) {
    var header = new HorizontalLayout();
    header.setWidthFull();
    header.setAlignItems(HorizontalLayout.Alignment.CENTER);
    header.setSpacing(true);
    header.getStyle()
            .set("margin-bottom", "var(--lumo-space-m)");

    var customerName = new H2(nullSafe(customer.getName()));
    customerName.getStyle()
            .set("margin", "0")
            .set("flex", "1");

    var editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT), e -> {
      var dialog = new CustomerDialog(customerService, c -> {
        updateCustomer(c);
        Notification.show("Customer updated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      });
      dialog.openUpdateForm(customer);
    });
    editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    var deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH), e -> {
      var confirmDialog = new ConfirmDialog();
      confirmDialog.setHeader("Delete Customer?");
      confirmDialog.setText("Are you sure you want to delete '" + customer.getName() + "' ?");
      confirmDialog.setCancelable(true);
      confirmDialog.setConfirmText("Delete");
      confirmDialog.setConfirmButtonTheme("error primary");
      confirmDialog.addConfirmListener(confirmEvent -> deleteCustomer(customer));
      confirmDialog.open();
    });
    deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

    header.add(customerName, editBtn, deleteBtn);
    return header;
  }

  private Card createBasicInfoCard(Customer customer) {
    Card card = new Card();
    card.setWidthFull();

    H3 title = new H3("Basic Information");
    title.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    card.add(
            title,
            createInfoRow(VaadinIcon.USER, "Customer Type",
                    customer.getType() != null ? customer.getType().toString() : "N/A"),
            createInfoRow(VaadinIcon.INVOICE, "GSTIN", nullSafe(customer.getGstin()))
    );

    return card;
  }

  private Card createContactInfoCard(Customer customer) {
    Card card = new Card();
    card.setWidthFull();

    H3 title = new H3("Contact Information");
    title.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    card.add(
            title,
            createInfoRow(VaadinIcon.ENVELOPE, "Email", nullSafe(customer.getEmail())),
            createInfoRow(VaadinIcon.PHONE, "Phone", nullSafe(customer.getPhone()))
    );
    return card;
  }

  private Card createAddressCard(Customer customer) {
    Card card = new Card();
    card.setWidthFull();

    H3 title = new H3("Address");
    title.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    card.add(
            title,
            createInfoRow(VaadinIcon.ROAD, "Street", nullSafe(customer.getStreet())),
            createInfoRow(VaadinIcon.BUILDING, "City", nullSafe(customer.getCity())),
            createInfoRow(VaadinIcon.MAP_MARKER, "State", customer.getState() != null ? customer.getState().name().replace('_', ' ') : "N/A"),
            createInfoRow(VaadinIcon.MAILBOX, "Postal Code", nullSafe(customer.getPostalCode()))
    );
    return card;
  }

  private HorizontalLayout createInfoRow(VaadinIcon iconType, String label, String value) {
    var row = new HorizontalLayout();
    row.setWidthFull();
    row.setAlignItems(HorizontalLayout.Alignment.START);
    row.setSpacing(true);
    row.getStyle()
            .set("margin-bottom", "var(--lumo-space-s)")
            .set("flex-wrap", "wrap");

    var icon = new Icon(iconType);
    icon.setSize("20px");
    icon.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin-top", "2px")
            .set("flex-shrink", "0");

    var labelSpan = new Span(label + ":");
    labelSpan.getStyle()
            .set("font-weight", "500")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("min-width", "120px")
            .set("flex-shrink", "0");

    var valueSpan = new Span(value);
    valueSpan.getStyle()
            .set("color", "var(--lumo-body-text-color)")
            .set("word-break", "break-word")
            .set("flex", "1");

    row.add(icon, labelSpan, valueSpan);
    return row;
  }

  private void addCustomer(Customer customer) {
    if (!dataProvider.getItems().contains(customer)) {
      dataProvider.getItems().add(customer);
      dataProvider.refreshAll();
    }
  }

  private void updateCustomer(Customer updated) {
    dataProvider.refreshAll();
    setDetail(createDetailContent(updated));
  }

  private void deleteCustomer(Customer customer) {
    try {
      customerService.deleteById(customer.getId());

      Notification.show("Customer deleted successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

      dataProvider.getItems().remove(customer);
      dataProvider.refreshAll();
      closeDetail();
    } catch (Exception ex) {
      var notification = Notification.show("Failed to delete customer: " + ex.getMessage());
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setDuration(5000);
    }
  }

  private void closeDetail() {
    setDetail(null);
    grid.asSingleSelect().clear();
  }

  private String nullSafe(String s) {
    return s == null ? "" : s;
  }
}
