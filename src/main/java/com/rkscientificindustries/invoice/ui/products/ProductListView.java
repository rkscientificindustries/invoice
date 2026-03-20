package com.rkscientificindustries.invoice.ui.products;

import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductService;
import com.rkscientificindustries.invoice.ui.MainLayout;
import com.rkscientificindustries.invoice.ui.utils.AppConstants;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.dao.DataIntegrityViolationException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.rkscientificindustries.invoice.ui.utils.InvoiceUtils.showNotification;

@PageTitle("Products")
@Route(value = "products", layout = MainLayout.class)
public class ProductListView extends MasterDetailLayout {
  private final Grid<Product> grid = new Grid<>(Product.class, false);
  private final ProductService productService;
  private final ProductDialog productDialog;
  private final ListDataProvider<Product> dataProvider;
  private final Map<Product, Integer> rowIndexMap = new HashMap<>();

  public ProductListView(ProductService productService) {
    this.productService = productService;
    this.productDialog = new ProductDialog(productService, this::addProduct);

    setSizeFull();

    // Create a master area with the grid
    var masterContent = new VerticalLayout();
    masterContent.setSizeFull();
    masterContent.setPadding(false);
    masterContent.setSpacing(false);

    grid.setSizeFull();

    var addBtn = new Button("Add Product", new Icon(VaadinIcon.PLUS), _ -> productDialog.openEmptyForm());
    addBtn.addThemeVariants(ButtonVariant.PRIMARY);

    var toolbar = new HorizontalLayout(addBtn);
    toolbar.setWidthFull();
    toolbar.setPadding(true);
    toolbar.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

    masterContent.add(toolbar, grid);
    masterContent.setFlexGrow(1, grid);

    setMaster(masterContent);

    // Initially hide detail
    setDetail(null);

    dataProvider = new ListDataProvider<>(new ArrayList<>(productService.findAll()));
    updateRowIndices();
    configureGrid();
    grid.setDataProvider(dataProvider);

    // Configure master-detail behavior
    setMasterMinSize(AppConstants.MASTER_MIN_WIDTH);
    setDetailSize(AppConstants.DETAIL_WIDTH_NARROW);

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
    for (Product product : dataProvider.getItems()) {
      rowIndexMap.put(product, index++);
    }
  }

  private void configureGridColumns() {
    grid.addThemeVariants(GridVariant.ROW_STRIPES);
    grid.addComponentColumn(product -> {
          var index = rowIndexMap.get(product);
          return new Span(index != null ? String.valueOf(index) : "");
        })
        .setHeader("#")
        .setFlexGrow(0)
        .setWidth(AppConstants.INDEX_COLUMN_WIDTH);

    grid.addColumn(Product::getName)
        .setHeader("Name")
        .setAutoWidth(true);

    grid.addColumn(product -> product.getUnit() != null ? product.getUnit().toString() : "")
        .setHeader("Unit")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.CENTER);

    grid.addColumn(new NumberRenderer<>(Product::getUnitPrice,
            NumberFormat.getCurrencyInstance(Locale.of("en", "IN"))))
        .setHeader("Unit Price")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END);
  }

  private void configureGridInteractions() {
    grid.addItemClickListener(clickEvent -> {
      grid.asSingleSelect().setValue(clickEvent.getItem());
      setDetail(createDetailContent(clickEvent.getItem()));
    });

    grid.addCellFocusListener(event -> event.getItem().ifPresent(product -> grid.asSingleSelect().setValue(product)));
    Shortcuts.addShortcutListener(grid,
        _ -> {
          var selected = grid.asSingleSelect().getValue();
          if (selected != null) {
            setDetail(createDetailContent(selected));
          }
        },
        Key.ENTER);
  }

  private VerticalLayout createDetailContent(Product product) {
    var content = new VerticalLayout();
    content.setSizeFull();
    content.setPadding(true);
    content.setSpacing(true);

    // Header with close button and actions
    var header = createDetailHeader(product);
    content.add(header);

    // Product information cards
    content.add(createBasicInfoCard(product));
    content.add(createTaxInfoCard(product));

    return content;
  }

  private HorizontalLayout createDetailHeader(Product product) {
    var header = new HorizontalLayout();
    header.setWidthFull();
    header.setAlignItems(HorizontalLayout.Alignment.CENTER);
    header.setSpacing(true);

    var productName = new H2(nullSafe(product.getName()));
    productName.getStyle()
        .set("flex", "1");

    var editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT), _ -> {
      var dialog = new ProductDialog(productService, updatedProduct -> {
        updateRowIndices();
        dataProvider.refreshAll();
        setDetail(createDetailContent(updatedProduct));
      });
      dialog.openUpdateForm(product);
    });
    editBtn.addThemeVariants(ButtonVariant.PRIMARY);

    var deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH), _ -> {
      var confirmDialog = new ConfirmDialog();
      confirmDialog.setHeader("Delete Product?");
      confirmDialog.setText("Are you sure you want to delete '" + product.getName() + "' ?");
      confirmDialog.setCancelable(true);
      confirmDialog.setConfirmText("Delete");
      confirmDialog.addConfirmListener(_ -> deleteProduct(product));
      confirmDialog.open();
    });
    deleteBtn.addThemeVariants(ButtonVariant.ERROR);

    header.add(productName, editBtn, deleteBtn);
    return header;
  }

  private Card createBasicInfoCard(Product product) {
    var card = new Card();
    card.setWidthFull();

    var title = new H3("Basic Information");
    var currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));

    card.add(
        title,
        createInfoRow(VaadinIcon.PACKAGE, "Product Name", nullSafe(product.getName())),
        createInfoRow(VaadinIcon.FILE_TEXT, "Description",
            product.getDescription() != null ? product.getDescription() : "N/A"),
        createInfoRow(VaadinIcon.BARCODE, "HSN Code", nullSafe(product.getHsnCode())),
        createInfoRow(VaadinIcon.SCALE, "Unit",
            product.getUnit() != null ? product.getUnit().toString() : "N/A"),
        createInfoRow(VaadinIcon.TAG, "Type",
            product.getType() != null ? product.getType().toString() : "N/A"),
        createInfoRow(VaadinIcon.MONEY, "Unit Price",
            product.getUnitPrice() != null ? currencyFormat.format(product.getUnitPrice()) : "₹0.00"),
        createToggleableInfoRow(VaadinIcon.MONEY_WITHDRAW, "Cost Price",
            product.getCostPrice() != null ? currencyFormat.format(product.getCostPrice()) : "₹0.00"),
        createToggleableInfoRow(VaadinIcon.USER, "Vendor Name",
            product.getVendorName() != null ? product.getVendorName() : "N/A")
    );

    return card;
  }

  private Card createTaxInfoCard(Product product) {
    var card = new Card();
    card.setWidthFull();

    var title = new H3("Tax Information");
    card.add(
        title,
        createInfoRow(VaadinIcon.PIGGY_BANK, "GST Rate",
            product.getGstRate() != null ? product.getGstRate() + "%" : "0%")
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

  private HorizontalLayout createToggleableInfoRow(VaadinIcon iconType, String label, String value) {
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

    // Create a masked value initially
    var valueSpan = new Span("•••••");
    valueSpan.getStyle()
        .set("word-break", "break-word")
        .set("flex", "1")
        .set("font-family", "monospace");

    // Create a toggle button with an eye icon
    var toggleBtn = new Button(new Icon(VaadinIcon.EYE_SLASH));
    toggleBtn.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
    toggleBtn.setTooltipText("Show " + label.toLowerCase());

    // Toggle functionality
    var isVisible = new boolean[]{false};
    toggleBtn.addClickListener(_ -> {
      if (isVisible[0]) {
        // Hide value
        valueSpan.setText("•••••");
        toggleBtn.setIcon(new Icon(VaadinIcon.EYE_SLASH));
        toggleBtn.setTooltipText("Show " + label.toLowerCase());
        valueSpan.getStyle().set("font-family", "monospace");
        isVisible[0] = false;
      } else {
        // Show value
        valueSpan.setText(value);
        toggleBtn.setIcon(new Icon(VaadinIcon.EYE));
        toggleBtn.setTooltipText("Hide " + label.toLowerCase());
        valueSpan.getStyle().remove("font-family");
        isVisible[0] = true;
      }
    });

    row.add(icon, labelSpan, valueSpan, toggleBtn);
    return row;
  }

  private void addProduct(Product product) {
    if (!dataProvider.getItems().contains(product)) {
      dataProvider.getItems().add(product);
      updateRowIndices();
      dataProvider.refreshAll();
    }
  }

  private void deleteProduct(Product product) {
    try {
      productService.deleteById(product.getId());
      showNotification("Product deleted successfully", NotificationVariant.SUCCESS);

      dataProvider.getItems().remove(product);
      updateRowIndices();
      dataProvider.refreshAll();
      closeDetail();
    } catch (DataIntegrityViolationException ex) {
      showNotification("Cannot delete this product because it is used by existing invoices.", NotificationVariant.ERROR);
    } catch (Exception ex) {
      showNotification("Failed to delete product: " + ex.getMessage(), NotificationVariant.ERROR);
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
