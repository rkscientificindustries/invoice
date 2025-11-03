package com.rkscientificindustries.invoice.ui.customers;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Consumer;

public class CustomerDetailPanel extends Div {
  private final CustomerService customerService;
  private final Consumer<Customer> onUpdate;
  private final Runnable onDelete;
  private final Runnable onClose;

  private H2 customerNameTitle;
  private Div basicInfoCard;
  private Div contactInfoCard;
  private Div addressCard;

  public CustomerDetailPanel(CustomerService customerService,
                             Consumer<Customer> onUpdate,
                             Runnable onDelete,
                             Runnable onClose) {
    this.customerService = customerService;
    this.onUpdate = onUpdate;
    this.onDelete = onDelete;
    this.onClose = onClose;

    setupStyles();
  }

  private void setupStyles() {
    addClassName("customer-detail-panel");

    getStyle()
            .set("width", "500px")
            .set("min-width", "500px")
            .set("max-width", "500px")
            .set("height", "100%")
            .set("background-color", "var(--lumo-base-color)")
            .set("border-left", "1px solid var(--lumo-contrast-10pct)")
            .set("box-shadow", "-2px 0 8px rgba(0, 0, 0, 0.15)")
            .set("overflow-y", "auto")
            .set("overflow-x", "hidden")
            .set("flex-shrink", "0");

    // Mobile responsive - take full width on small devices
    getElement().executeJs("""
            const panel = this;
            const updatePanelWidth = () => {
              if (window.innerWidth <= 768) {
                panel.style.minWidth = '100%';
                panel.style.maxWidth = '100%';
                panel.style.width = '100%';
              } else {
                panel.style.minWidth = '500px';
                panel.style.maxWidth = '500px';
                panel.style.width = '500px';
              }
            };
            updatePanelWidth();
            window.addEventListener('resize', updatePanelWidth);
            """);
  }

  public void showCustomer(Customer customer) {
    removeAll();

    var content = new VerticalLayout();
    content.setSizeFull();
    content.setPadding(true);
    content.setSpacing(true);
    content.getStyle()
            .set("padding", "var(--lumo-space-m)");

    // Header with close button and actions
    var header = createHeader(customer);
    content.add(header);

    // Main content area - Responsive FlexLayout
    var contentLayout = new FlexLayout();
    contentLayout.setWidthFull();
    contentLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
    contentLayout.getStyle()
            .set("gap", "var(--lumo-space-m)");

    // Create info cards
    basicInfoCard = createBasicInfoCard(customer);
    contactInfoCard = createContactInfoCard(customer);
    addressCard = createAddressCard(customer);

    var column = new VerticalLayout();
    column.setSpacing(true);
    column.setPadding(false);
    column.setWidthFull();

    column.add(basicInfoCard, contactInfoCard, addressCard);
    contentLayout.add(column);

    content.add(contentLayout);
    add(content);
  }

  public void close() {
    onClose.run();
  }

  private HorizontalLayout createHeader(Customer customer) {
    var header = new HorizontalLayout();
    header.setWidthFull();
    header.setAlignItems(HorizontalLayout.Alignment.CENTER);
    header.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "var(--lumo-space-s)")
            .set("margin-bottom", "var(--lumo-space-m)");

    // Close button
    var closeBtn = new Button(new Icon(VaadinIcon.CLOSE), e -> {
      close();
      onClose.run();
    });
    closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeBtn.getStyle().set("margin-right", "var(--lumo-space-s)");

    customerNameTitle = new H2(nullSafe(customer.getName()));
    customerNameTitle.getStyle()
            .set("margin", "0")
            .set("flex", "1");

    var actions = new HorizontalLayout();
    actions.setSpacing(true);

    var editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT), e -> {
      var dialog = new CustomerDialog(customerService, c -> {
        Notification.show("Customer updated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        updateDetails(c);
        onUpdate.accept(c);
      });
      dialog.openUpdateForm(customer);
    });
    editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    var deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH), e -> {
      confirmAndDeleteCustomer(customer);
    });
    deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

    actions.add(editBtn, deleteBtn);

    var topRow = new HorizontalLayout(closeBtn, customerNameTitle);
    topRow.setWidthFull();
    topRow.setAlignItems(HorizontalLayout.Alignment.CENTER);

    var wrapper = new VerticalLayout(topRow, actions);
    wrapper.setWidthFull();
    wrapper.setPadding(false);
    wrapper.setSpacing(true);

    return new HorizontalLayout(wrapper);
  }

  private void confirmAndDeleteCustomer(Customer customer) {
    var confirmDialog = new ConfirmDialog();
    confirmDialog.setHeader("Delete Customer?");
    confirmDialog.setText("Are you sure you want to delete '" + customer.getName() + "' ?");

    confirmDialog.setCancelable(true);
    confirmDialog.setConfirmText("Delete");
    confirmDialog.setConfirmButtonTheme("error primary");

    confirmDialog.addConfirmListener(event -> {
      try {
        customerService.deleteById(customer.getId());

        var notification = Notification.show("Customer deleted successfully");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);

        close();
        onDelete.run();
      } catch (Exception ex) {
        var notification = Notification.show("Failed to delete customer: " + ex.getMessage());
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setDuration(5000);
      }
    });

    confirmDialog.open();
  }

  private Div createBasicInfoCard(Customer customer) {
    var card = createCard("Basic Information");

    card.add(createInfoRow(VaadinIcon.USER, "Customer Type",
            customer.getType() != null ? customer.getType().toString() : "N/A"));
    card.add(createInfoRow(VaadinIcon.INVOICE, "GSTIN", nullSafe(customer.getGstin())));

    return card;
  }

  private Div createContactInfoCard(Customer customer) {
    var card = createCard("Contact Information");

    card.add(createInfoRow(VaadinIcon.ENVELOPE, "Email", nullSafe(customer.getEmail())));
    card.add(createInfoRow(VaadinIcon.PHONE, "Phone", nullSafe(customer.getPhone())));

    return card;
  }

  private Div createAddressCard(Customer customer) {
    var card = createCard("Address");

    card.add(createInfoRow(VaadinIcon.ROAD, "Street", nullSafe(customer.getStreet())));
    card.add(createInfoRow(VaadinIcon.BUILDING, "City", nullSafe(customer.getCity())));
    card.add(createInfoRow(VaadinIcon.MAP_MARKER, "State",
            customer.getState() != null ? customer.getState().name().replace('_', ' ') : "N/A"));
    card.add(createInfoRow(VaadinIcon.MAILBOX, "Postal Code", nullSafe(customer.getPostalCode())));

    return card;
  }

  private Div createCard(String title) {
    var card = new Div();
    card.setWidthFull();
    card.getStyle()
            .set("background-color", "var(--lumo-base-color)")
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("box-sizing", "border-box");

    var cardTitle = new H3(title);
    cardTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    card.add(cardTitle);
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

  private void updateDetails(Customer updatedCustomer) {
    // Update the title
    if (customerNameTitle != null) {
      customerNameTitle.setText(nullSafe(updatedCustomer.getName()));
    }

    // Update basic info card
    if (basicInfoCard != null) {
      updateBasicInfoCard(updatedCustomer);
    }

    // Update contact info card
    if (contactInfoCard != null) {
      updateContactInfoCard(updatedCustomer);
    }

    // Update address card
    if (addressCard != null) {
      updateAddressCard(updatedCustomer);
    }
  }

  private void updateBasicInfoCard(Customer customer) {
    basicInfoCard.removeAll();

    var cardTitle = new H3("Basic Information");
    cardTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    basicInfoCard.add(cardTitle);
    basicInfoCard.add(createInfoRow(VaadinIcon.USER, "Customer Type",
            customer.getType() != null ? customer.getType().toString() : "N/A"));
    basicInfoCard.add(createInfoRow(VaadinIcon.INVOICE, "GSTIN", nullSafe(customer.getGstin())));
  }

  private void updateContactInfoCard(Customer customer) {
    contactInfoCard.removeAll();

    var cardTitle = new H3("Contact Information");
    cardTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    contactInfoCard.add(cardTitle);
    contactInfoCard.add(createInfoRow(VaadinIcon.ENVELOPE, "Email", nullSafe(customer.getEmail())));
    contactInfoCard.add(createInfoRow(VaadinIcon.PHONE, "Phone", nullSafe(customer.getPhone())));
  }

  private void updateAddressCard(Customer customer) {
    addressCard.removeAll();

    var cardTitle = new H3("Address");
    cardTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    addressCard.add(cardTitle);
    addressCard.add(createInfoRow(VaadinIcon.ROAD, "Street", nullSafe(customer.getStreet())));
    addressCard.add(createInfoRow(VaadinIcon.BUILDING, "City", nullSafe(customer.getCity())));
    addressCard.add(createInfoRow(VaadinIcon.MAP_MARKER, "State",
            customer.getState() != null ? customer.getState().name().replace('_', ' ') : "N/A"));
    addressCard.add(createInfoRow(VaadinIcon.MAILBOX, "Postal Code", nullSafe(customer.getPostalCode())));
  }

  private String nullSafe(String s) {
    return s == null ? "" : s;
  }
}
