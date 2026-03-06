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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;

@PageTitle("Customers")
@Route(value = "customers", layout = MainLayout.class)
public class CustomerListView extends MasterDetailLayout {
  private final Grid<Customer> grid = new Grid<>(Customer.class, false);
  private final CustomerService customerService;
  private final CustomerDialog customerDialog;
  private final ListDataProvider<Customer> dataProvider;
  private final Map<Customer, Integer> rowIndexMap = new HashMap<>();

  public CustomerListView(CustomerService customerService) {
    this.customerService = customerService;
    this.customerDialog = new CustomerDialog(customerService, this::addCustomer);

    setSizeFull();

    // Create a master area with the grid
    var masterContent = new VerticalLayout();
    masterContent.setSizeFull();
    masterContent.setPadding(false);
    masterContent.setSpacing(false);

    grid.setSizeFull();
    masterContent.add(grid);
    masterContent.setFlexGrow(1, grid);

    var addCustomerBtn = FabButton.create("Add Customer", _ -> customerDialog.openEmptyForm());
    masterContent.add(addCustomerBtn);

    setMaster(masterContent);

    // Initially hide detail
    setDetail(null);

    dataProvider = new ListDataProvider<>(customerService.findAll());
    updateRowIndices();
    configureGrid();
    grid.setDataProvider(dataProvider);

    // Configure master-detail behavior
    setMasterMinSize("400px");
    setDetailSize("500px");

    // Hide detail on a backdrop click and escape the key
    addBackdropClickListener(_ -> closeDetail());
    addDetailEscapePressListener(_ -> closeDetail());
  }

  private void configureGrid() {
    configureGridColumns();
    configureGridInteractions();
  }

  private void updateRowIndices() {
    rowIndexMap.clear();
    int index = 1;
    for (Customer customer : dataProvider.getItems()) {
      rowIndexMap.put(customer, index++);
    }
  }

  private void configureGridColumns() {
    grid.addComponentColumn(customer -> {
          var index = rowIndexMap.get(customer);
          return new Span(index != null ? String.valueOf(index) : "");
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

    grid.addCellFocusListener(e -> e.getItem().ifPresent(c -> grid.asSingleSelect().setValue(c)));
    Shortcuts.addShortcutListener(grid,
        _ -> {
          var selected = grid.asSingleSelect().getValue();
          if (selected != null) setDetail(createDetailContent(selected));
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

    var customerName = new H2(nullSafe(customer.getName()));
    customerName.getStyle()
        .set("flex", "1");

    var editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT), _ -> {
      var dialog = new CustomerDialog(customerService, updatedCustomer -> {
        updateRowIndices();
        dataProvider.refreshAll();
        setDetail(createDetailContent(updatedCustomer));
      });
      dialog.openUpdateForm(customer);
    });
    editBtn.addThemeVariants(ButtonVariant.PRIMARY);

    var deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH), _ -> {
      var confirmDialog = new ConfirmDialog();
      confirmDialog.setHeader("Delete Customer?");
      confirmDialog.setText("Are you sure you want to delete '" + customer.getName() + "' ?");
      confirmDialog.setCancelable(true);
      confirmDialog.setConfirmText("Delete");
      confirmDialog.addConfirmListener(_ -> deleteCustomer(customer));
      confirmDialog.open();
    });
    deleteBtn.addThemeVariants(ButtonVariant.ERROR);

    header.add(customerName, editBtn, deleteBtn);
    return header;
  }

  private Card createBasicInfoCard(Customer customer) {
    var card = new Card();
    card.setWidthFull();
    var title = new H3("Basic Information");
    card.add(
        title,
        createInfoRow(VaadinIcon.USER, "Customer Type", customer.getType() != null ? customer.getType().name() : ""),
        createInfoRow(VaadinIcon.INVOICE, "GSTIN", nullSafe(customer.getGstin()))
    );
    return card;
  }

  private Card createContactInfoCard(Customer customer) {
    var card = new Card();
    card.setWidthFull();

    var title = new H3("Contact Information");
    card.add(
        title,
        createInfoRow(VaadinIcon.ENVELOPE, "Email", nullSafe(customer.getEmail())),
        createInfoRow(VaadinIcon.PHONE, "Phone", nullSafe(customer.getPhone()))
    );
    return card;
  }

  private Card createAddressCard(Customer customer) {
    var card = new Card();
    card.setWidthFull();

    var title = new H3("Address");
    card.add(
        title,
        createInfoRow(VaadinIcon.ROAD, "Street", nullSafe(customer.getStreet())),
        createInfoRow(VaadinIcon.BUILDING, "City", nullSafe(customer.getCity())),
        createInfoRow(VaadinIcon.MAP_MARKER, "State", customer.getState() != null ? customer.getState().name() : ""),
        createInfoRow(VaadinIcon.MAILBOX, "Postal Code", nullSafe(customer.getPostalCode()))
    );
    return card;
  }

  private HorizontalLayout createInfoRow(VaadinIcon iconType, String label, String value) {
    var row = new HorizontalLayout();
    row.setWidthFull();
    row.setAlignItems(HorizontalLayout.Alignment.START);
    row.setSpacing(true);

    var icon = new Icon(iconType);
    var labelSpan = new Span(label + ":");
    labelSpan.getStyle()
        .set("font-weight", "500")
        .set("min-width", "120px")
        .set("flex-shrink", "0");

    var valueSpan = new Span(value);
    valueSpan.getStyle()
        .set("word-break", "break-word")
        .set("flex", "1");

    row.add(icon, labelSpan, valueSpan);
    return row;
  }

  private void addCustomer(Customer customer) {
    if (!dataProvider.getItems().contains(customer)) {
      dataProvider.getItems().add(customer);
      updateRowIndices();
      dataProvider.refreshAll();
    }
  }

  private void deleteCustomer(Customer customer) {
    try {
      customerService.deleteById(customer.getId());
      var notification = Notification.show("Customer deleted successfully");
      notification.addThemeVariants(NotificationVariant.SUCCESS);
      notification.setPosition(Notification.Position.BOTTOM_CENTER);
      dataProvider.getItems().remove(customer);
      updateRowIndices();
      dataProvider.refreshAll();
      closeDetail();
    } catch (DataIntegrityViolationException ex) {
      var notification = Notification.show("Cannot delete this customer because they have existing invoices.");
      notification.addThemeVariants(NotificationVariant.ERROR);
      notification.setPosition(Notification.Position.BOTTOM_CENTER);
    } catch (Exception ex) {
      var notification = Notification.show("Failed to delete customer: " + ex.getMessage());
      notification.addThemeVariants(NotificationVariant.ERROR);
      notification.setPosition(Notification.Position.BOTTOM_CENTER);
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
