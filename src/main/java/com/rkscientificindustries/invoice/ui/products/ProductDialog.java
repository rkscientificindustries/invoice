package com.rkscientificindustries.invoice.ui.products;

import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductService;
import com.rkscientificindustries.invoice.ui.utils.AppConstants;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static com.rkscientificindustries.invoice.ui.utils.InvoiceUtils.showNotification;

public class ProductDialog extends Dialog {
  private final TextField name = new TextField("Product Name");
  private final TextArea description = new TextArea("Description");
  private final TextField hsnCode = new TextField("HSN Code");
  private final ComboBox<Product.Unit> unit = new ComboBox<>("Unit", Product.Unit.values());
  private final BigDecimalField unitPrice = new BigDecimalField("Unit Price (₹)");
  private final BigDecimalField costPrice = new BigDecimalField("Cost Price (₹)");
  private final TextField vendorName = new TextField("Vendor Name");
  private final ComboBox<Product.ItemType> type = new ComboBox<>("Type", Product.ItemType.values());
  private final ComboBox<BigDecimal> gstRate = new ComboBox<>("GST Rate (%)");

  private final BeanValidationBinder<Product> binder = new BeanValidationBinder<>(Product.class);
  private final FormLayout formLayout = new FormLayout();
  private final ProductService productService;
  private final Consumer<Product> onSaved;

  public ProductDialog(ProductService productService, Consumer<Product> onSaved) {
    this.productService = productService;
    this.onSaved = onSaved;
    setWidth(AppConstants.DIALOG_WIDTH);
    setDraggable(true);

    description.setMaxLength(500);
    unitPrice.setPrefixComponent(new Span("₹"));
    costPrice.setPrefixComponent(new Span("₹"));

    gstRate.setItems(BigDecimal.valueOf(5), BigDecimal.valueOf(18));
    gstRate.setItemLabelGenerator(rate -> rate + "%");

    formLayout.add(name, description, hsnCode, unit, unitPrice, costPrice, vendorName, type, gstRate);
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
    setHeaderTitle("Add Product");

    var product = new Product();
    binder.setReadOnly(false);
    binder.readBean(product);

    // Apply defaults
    unit.setValue(Product.Unit.PCS);
    type.setValue(Product.ItemType.BO);
    gstRate.setValue(AppConstants.DEFAULT_GST_RATE);
    unitPrice.setValue(BigDecimal.ZERO);
    costPrice.setValue(BigDecimal.ZERO);

    var saveBtn = new Button("Save", _ -> {
      if (binder.writeBeanIfValid(product)) {
        try {
          close();
          var saved = productService.save(product);
          showNotification("Product created successfully", NotificationVariant.SUCCESS);
          onSaved.accept(saved);
        } catch (Exception ex) {
          showNotification("Failed to save: " + ex.getMessage(), NotificationVariant.ERROR);
        }
      }
    });
    saveBtn.addThemeVariants(ButtonVariant.PRIMARY);
    saveBtn.addClickShortcut(Key.ENTER);

    var cancelBtn = new Button("Cancel", _ -> {
      binder.readBean(product);
      close();
    });
    cancelBtn.addClickShortcut(Key.ESCAPE);

    getFooter().add(cancelBtn, saveBtn);
    add(formLayout);
    open();
  }

  public void openUpdateForm(Product product) {
    removeAll();
    getFooter().removeAll();
    setHeaderTitle("Edit Product");

    binder.setReadOnly(false);
    binder.readBean(product);

    var updateBtn = new Button("Update", _ -> {
      if (binder.writeBeanIfValid(product)) {
        try {
          var saved = productService.save(product);
          onSaved.accept(saved);
          close();
        } catch (Exception ex) {
          showNotification("Failed to save: " + ex.getMessage(), NotificationVariant.ERROR);
        }
      }
    });
    updateBtn.addThemeVariants(ButtonVariant.PRIMARY);
    updateBtn.addClickShortcut(Key.ENTER);

    var cancelBtn = new Button("Cancel", _ -> {
      binder.readBean(product);
      close();
    });
    cancelBtn.addClickShortcut(Key.ESCAPE);

    getFooter().add(cancelBtn, updateBtn);
    add(formLayout);
    open();
  }
}
