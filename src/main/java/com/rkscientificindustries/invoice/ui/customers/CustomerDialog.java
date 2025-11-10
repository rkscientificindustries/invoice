package com.rkscientificindustries.invoice.ui.customers;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.customer.CustomerType;
import com.rkscientificindustries.invoice.backend.utils.State;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.util.function.Consumer;

public class CustomerDialog extends Dialog {
  private final TextField name = new TextField("Name");
  private final TextField email = new TextField("Email");
  private final TextField phone = new TextField("Phone");
  private final TextField gstin = new TextField("GSTIN");
  private final ComboBox<CustomerType> type = new ComboBox<>("Type", CustomerType.values());
  private final TextField street = new TextField("Street");
  private final TextField city = new TextField("City");
  private final ComboBox<State> state = new ComboBox<>("State", State.values());
  private final TextField postalCode = new TextField("Postal Code");

  private final BeanValidationBinder<Customer> binder = new BeanValidationBinder<>(Customer.class);
  private final FormLayout formLayout = new FormLayout();
  private final CustomerService customerService;
  private final Consumer<Customer> onSaved;

  public CustomerDialog(CustomerService customerService, Consumer<Customer> onSaved) {
    this.customerService = customerService;
    this.onSaved = onSaved;
    setWidth("720px");
    setModal(true);
    setDraggable(true);

    formLayout.add(name, email, phone, gstin, type, street, city, state, postalCode);

    binder.bindInstanceFields(this);

    type.setValue(CustomerType.BUSINESS);
    state.setValue(State.HARYANA);
    state.setItemLabelGenerator(s -> s.name().replace('_', ' '));
  }

  public void openEmptyForm() {
    removeAll();
    getFooter().removeAll();
    setHeaderTitle("Add Customer");

    var customer = new Customer();
    // Ensure fields are editable and cleared for a fresh entry
    binder.setReadOnly(false);
    binder.readBean(customer);
    // Re-apply sensible defaults
    type.setValue(CustomerType.BUSINESS);
    state.setValue(State.HARYANA);

    var saveBtn = new Button("Save", event -> {
      if (binder.writeBeanIfValid(customer)) {
        close();
        var notification = Notification.show("Customer created successfully");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.BOTTOM_CENTER);
        onSaved.accept(customerService.save(customer));
      }
    });
    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveBtn.addClickShortcut(Key.ENTER);
    var cancelBtn = new Button("Cancel", event -> {
      binder.readBean(customer);
      close();
    });

    getFooter().add(cancelBtn, saveBtn);

    add(formLayout);
    open();
  }

  public void openUpdateForm(Customer customer) {
    removeAll();
    getFooter().removeAll();
    setHeaderTitle("Edit Customer");

    // Ensure fields are editable
    binder.setReadOnly(false);
    binder.readBean(customer);

    var updateBtn = new Button("Update", event -> {
      if (binder.writeBeanIfValid(customer)) {
        close();
        onSaved.accept(customerService.save(customer));
      }
    });
    updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    updateBtn.addClickShortcut(Key.ENTER);

    var cancelBtn = new Button("Cancel", event -> {
      binder.readBean(customer); // Reset to original values
      close();
    });
    cancelBtn.addClickShortcut(Key.ESCAPE);

    getFooter().add(cancelBtn, updateBtn);
    add(formLayout);
    open();
  }

  private void setFieldsReadOnly(Customer customer) {
    name.setValue(customer.getName());
    email.setValue(customer.getEmail());
    phone.setValue(customer.getPhone());
    gstin.setValue(customer.getGstin());
    type.setValue(customer.getType());
    street.setValue(customer.getStreet());
    city.setValue(customer.getCity());
    state.setValue(customer.getState());
    postalCode.setValue(customer.getPostalCode());
    binder.setReadOnly(true);
  }

  private static String formatCustomerDetails(Customer c) {
    return String.format("""
        Name: %s
        Email: %s
        Phone: %s
        GSTIN: %s
        Type: %s
        Street: %s
        City: %s
        State: %s
        Postal Code: %s""",
        c.getName(),
        c.getEmail(),
        c.getPhone(),
        c.getGstin(),
        c.getType(),
        c.getStreet(),
        c.getCity(),
        c.getState().name().replace('_', ' '),
        c.getPostalCode());
  }
}
