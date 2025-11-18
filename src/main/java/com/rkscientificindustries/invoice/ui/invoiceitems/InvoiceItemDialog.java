package com.rkscientificindustries.invoice.ui.invoiceitems;

import com.rkscientificindustries.invoice.backend.invoice.InvoiceItem;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceItemService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class InvoiceItemDialog extends Dialog {
  private final TextField name = new TextField("Item Name");
  private final TextArea description = new TextArea("Description");
  private final TextField hsnCode = new TextField("HSN Code");
  private final ComboBox<InvoiceItem.Unit> unit = new ComboBox<>("Unit", InvoiceItem.Unit.values());
  private final BigDecimalField unitPrice = new BigDecimalField("Unit Price (₹)");
  private final BigDecimalField costPrice = new BigDecimalField("Cost Price (₹)");
  private final TextField vendorName = new TextField("Vendor Name");
  private final ComboBox<InvoiceItem.ItemType> type = new ComboBox<>("Type", InvoiceItem.ItemType.values());
  private final ComboBox<BigDecimal> gst = new ComboBox<>("GST Rate (%)");

  private final BeanValidationBinder<InvoiceItem> binder = new BeanValidationBinder<>(InvoiceItem.class);
  private final FormLayout formLayout = new FormLayout();
  private final InvoiceItemService invoiceItemService;
  private final Consumer<InvoiceItem> onSaved;

  public InvoiceItemDialog(InvoiceItemService invoiceItemService, Consumer<InvoiceItem> onSaved) {
    this.invoiceItemService = invoiceItemService;
    this.onSaved = onSaved;
    setWidth("720px");
    setModal(true);
    setDraggable(true);

    // Configure field hints
    hsnCode.setHelperText("Enter 4 or 6 digit HSN code");
    description.setMaxLength(200);
    description.setHelperText("Max 200 characters");
    unitPrice.setPrefixComponent(new com.vaadin.flow.component.html.Span("₹"));
    costPrice.setPrefixComponent(new com.vaadin.flow.component.html.Span("₹"));

    // Configure GST dropdown with only 5% and 18%
    gst.setItems(BigDecimal.valueOf(5), BigDecimal.valueOf(18));
    gst.setItemLabelGenerator(rate -> rate + "%");

    // Set default values
    unit.setValue(InvoiceItem.Unit.PCS);
    type.setValue(InvoiceItem.ItemType.BO);
    gst.setValue(BigDecimal.valueOf(18));
    unitPrice.setValue(BigDecimal.ZERO);
    costPrice.setValue(BigDecimal.ZERO);

    formLayout.add(name, description, hsnCode, unit, unitPrice, costPrice, vendorName, type, gst);
    formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
    );
    formLayout.setColspan(description, 2);

    binder.bindInstanceFields(this);
  }

  public void openEmptyForm() {
    removeAll();
    getFooter().removeAll();
    setHeaderTitle("Add Invoice Item");

    var item = new InvoiceItem();
    binder.setReadOnly(false);
    binder.readBean(item);

    // Re-apply defaults
    unit.setValue(InvoiceItem.Unit.PCS);
    type.setValue(InvoiceItem.ItemType.BO);
    gst.setValue(BigDecimal.valueOf(18));
    unitPrice.setValue(BigDecimal.ZERO);
    costPrice.setValue(BigDecimal.ZERO);

    var saveBtn = new Button("Save", event -> {
      if (binder.writeBeanIfValid(item)) {
        try {
          close();
          var saved = invoiceItemService.save(item);
          Notification.show("Invoice item created successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
          onSaved.accept(saved);
        } catch (Exception ex) {
          var notification = Notification.show("Failed to save: " + ex.getMessage());
          notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
          notification.setDuration(5000);
        }
      }
    });
    saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveBtn.addClickShortcut(Key.ENTER);

    var cancelBtn = new Button("Cancel", event -> {
      binder.readBean(item);
      close();
    });
    cancelBtn.addClickShortcut(Key.ESCAPE);

    getFooter().add(cancelBtn, saveBtn);
    add(formLayout);
    open();
  }

  public void openUpdateForm(InvoiceItem item) {
    removeAll();
    getFooter().removeAll();
    setHeaderTitle("Edit Invoice Item");

    binder.setReadOnly(false);
    binder.readBean(item);

    var updateBtn = new Button("Update", event -> {
      if (binder.writeBeanIfValid(item)) {
        try {
          close();
          var saved = invoiceItemService.save(item);
          onSaved.accept(saved);
        } catch (Exception ex) {
          var notification = Notification.show("Failed to update: " + ex.getMessage());
          notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
          notification.setDuration(5000);
        }
      }
    });
    updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    updateBtn.addClickShortcut(Key.ENTER);

    var cancelBtn = new Button("Cancel", event -> {
      binder.readBean(item);
      close();
    });
    cancelBtn.addClickShortcut(Key.ESCAPE);

    getFooter().add(cancelBtn, updateBtn);
    add(formLayout);
    open();
  }
}
