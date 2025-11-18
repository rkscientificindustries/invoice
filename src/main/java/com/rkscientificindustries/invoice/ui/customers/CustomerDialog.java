package com.rkscientificindustries.invoice.ui.customers;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.customer.CustomerType;
import com.rkscientificindustries.invoice.backend.utils.State;
import com.vaadin.flow.component.Key;
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
        Notification.show("Customer created successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
}
