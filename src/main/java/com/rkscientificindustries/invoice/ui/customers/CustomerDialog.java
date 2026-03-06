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
    setDraggable(true);

    state.setItemLabelGenerator(s -> s.name().replace('_', ' '));
    formLayout.add(name, email, phone, gstin, type, street, city, state, postalCode);
    binder.bindInstanceFields(this);
  }

  public void openEmptyForm() {
    removeAll();
    getFooter().removeAll();
    setHeaderTitle("Add Customer");

    var customer = new Customer();
    binder.setReadOnly(false);
    binder.readBean(customer);

    type.setValue(CustomerType.BUSINESS);
    state.setValue(State.HARYANA);

    var saveBtn = new Button("Save", _ -> {
      if (binder.writeBeanIfValid(customer)) {
        try {
          var saved = customerService.save(customer);
          onSaved.accept(saved);
          var notification = Notification.show("Customer created successfully");
          notification.addThemeVariants(NotificationVariant.SUCCESS);
          notification.setPosition(Notification.Position.BOTTOM_CENTER);
          close();
        } catch (Exception ex) {
          var notification = Notification.show("Failed to save: " + ex.getMessage());
          notification.addThemeVariants(NotificationVariant.ERROR);
          notification.setPosition(Notification.Position.BOTTOM_CENTER);
        }
      }
    });
    saveBtn.addThemeVariants(ButtonVariant.PRIMARY);
    saveBtn.addClickShortcut(Key.ENTER);
    var cancelBtn = new Button("Cancel", _ -> close());
    cancelBtn.addClickShortcut(Key.ESCAPE);

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

    var updateBtn = new Button("Update", _ -> {
      if (binder.writeBeanIfValid(customer)) {
        try {
          var savedCustomer = customerService.save(customer);
          onSaved.accept(savedCustomer);
          var notification = Notification.show("Customer updated successfully");
          notification.addThemeVariants(NotificationVariant.SUCCESS);
          notification.setPosition(Notification.Position.BOTTOM_CENTER);
          close();
        } catch (Exception ex) {
          var notification = Notification.show("Failed to update: " + ex.getMessage());
          notification.addThemeVariants(NotificationVariant.ERROR);
          notification.setPosition(Notification.Position.BOTTOM_CENTER);
        }
      }
    });
    updateBtn.addThemeVariants(ButtonVariant.PRIMARY);
    updateBtn.addClickShortcut(Key.ENTER);

    var cancelBtn = new Button("Cancel", _ -> {
      binder.readBean(customer); // Reset to original values
      close();
    });
    cancelBtn.addClickShortcut(Key.ESCAPE);

    getFooter().add(cancelBtn, updateBtn);
    add(formLayout);
    open();
  }
}
