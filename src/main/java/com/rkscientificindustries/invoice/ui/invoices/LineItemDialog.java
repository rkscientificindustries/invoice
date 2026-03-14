package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.shared.Registration;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

public class LineItemDialog extends Dialog {
  private final ProductService productService;
  private final ComboBox<Product> productCombo = new ComboBox<>("Product");
  private final IntegerField quantityField = new IntegerField("Quantity");
  private final NumberField priceField = new NumberField("Unit Price");
  private final Button saveBtn = new Button();

  private Registration saveListener;
  private Invoice.LineItem currentLine;
  private boolean currentIsNew;
  private Consumer<Invoice.LineItem> currentOnSave;

  public LineItemDialog(ProductService productService) {
    this.productService = productService;
    setCloseOnEsc(true);
    setDraggable(true);
    setResizable(false);

    productCombo.setWidthFull();
    productCombo.setPlaceholder("Select a product");
    productCombo.setItems(
        query -> productService.findByName(query.getFilter().orElse(""),
            PageRequest.of(query.getPage(), query.getLimit())).stream(),
        query -> productService.countByName(query.getFilter().orElse(""))
    );
    productCombo.setItemLabelGenerator(Product::getName);
    productCombo.addValueChangeListener(e -> {
      if (e.getValue() != null && e.getValue().getUnitPrice() != null) {
        priceField.setValue(e.getValue().getUnitPrice().doubleValue());
      }
    });
    quantityField.setStepButtonsVisible(true);
    quantityField.setMin(1);
    priceField.setMin(0);

    var horizontalLayout = new HorizontalLayout(priceField, quantityField);
    var layout = new VerticalLayout(productCombo, horizontalLayout);
    add(layout);

    saveBtn.addThemeVariants(ButtonVariant.PRIMARY);
    saveBtn.addClickShortcut(Key.ENTER);
    var cancel = new Button("Cancel", _ -> close());
    getFooter().add(cancel, saveBtn);
  }

  public void open(Invoice.LineItem lineItem, boolean isNew, Consumer<Invoice.LineItem> onSave) {
    this.currentLine = lineItem;
    this.currentIsNew = isNew;
    this.currentOnSave = onSave;

    setHeaderTitle(isNew ? "Add Item" : "Edit Item");
    saveBtn.setText(isNew ? "Add" : "Update");

    productCombo.clear();
    quantityField.clear();
    priceField.clear();
    quantityField.setValue(lineItem.getQuantity() != null ? lineItem.getQuantity() : 1);

    if (lineItem.getProductId() != null) {
      productService.findById(lineItem.getProductId()).ifPresent(productCombo::setValue);
    }
    if (lineItem.getUnitPrice() != null) {
      priceField.setValue(lineItem.getUnitPrice().doubleValue());
    }

    if (saveListener != null) saveListener.remove();
    saveListener = saveBtn.addClickListener(_ -> handleSave());
    open();
  }

  private void handleSave() {
    var product = productCombo.getValue();
    if (product == null || quantityField.getValue() == null || priceField.getValue() == null) return;

    currentLine.setProductId(product.getId());
    currentLine.setQuantity(quantityField.getValue());

    var unitPrice = BigDecimal.valueOf(priceField.getValue());
    var quantity = BigDecimal.valueOf(quantityField.getValue());
    var lineTotal = unitPrice.multiply(quantity);
    var gstRate = product.getGstRate();
    var taxAmount = lineTotal.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

    currentLine.setUnitPrice(unitPrice);
    currentLine.setGstRate(gstRate);
    currentLine.setTaxAmount(taxAmount);
    currentLine.setTotalAmount(lineTotal.add(taxAmount));

    currentOnSave.accept(currentLine);
    close();

    if (currentIsNew) resetFields();
  }

  private void resetFields() {
    productCombo.clear();
    quantityField.clear();
    priceField.clear();
  }
}
