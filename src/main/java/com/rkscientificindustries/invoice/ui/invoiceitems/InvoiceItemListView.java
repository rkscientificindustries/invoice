package com.rkscientificindustries.invoice.ui.invoiceitems;

import com.rkscientificindustries.invoice.backend.invoice.InvoiceItem;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceItemService;
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
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
import java.util.Locale;

@PageTitle("Invoice Items")
@Route(value = "invoice-items", layout = MainLayout.class)
public class InvoiceItemListView extends MasterDetailLayout {
  private final Grid<InvoiceItem> grid = new Grid<>(InvoiceItem.class, false);
  private final InvoiceItemService invoiceItemService;
  private final InvoiceItemDialog detailsDialog;
  private final ListDataProvider<InvoiceItem> dataProvider;

  public InvoiceItemListView(InvoiceItemService invoiceItemService) {
    this.invoiceItemService = invoiceItemService;
    this.detailsDialog = new InvoiceItemDialog(invoiceItemService, this::addInvoiceItem);

    setSizeFull();

    // Create master area with grid
    var masterContent = new VerticalLayout();
    masterContent.setSizeFull();
    masterContent.setPadding(false);
    masterContent.setSpacing(false);

    grid.setSizeFull();
    masterContent.add(grid);
    masterContent.setFlexGrow(1, grid);

    var addItemBtn = FabButton.create("Add Invoice Item", e -> detailsDialog.openEmptyForm());
    masterContent.add(addItemBtn);

    setMaster(masterContent);

    // Initially hide detail
    setDetail(null);

    configureGrid();
    dataProvider = new ListDataProvider<>(invoiceItemService.findAll());
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
    grid.addComponentColumn(item -> {
              int index = dataProvider.getItems().stream()
                      .toList()
                      .indexOf(item) + 1;
              return new Span(String.valueOf(index));
            })
            .setHeader("#")
            .setFlexGrow(0)
            .setWidth("64px");

    grid.addColumn(InvoiceItem::getName)
            .setHeader("Name")
            .setAutoWidth(true);

    grid.addColumn(item -> item.getUnit() != null ? item.getUnit().toString() : "")
            .setHeader("Unit")
            .setAutoWidth(true)
            .setTextAlign(ColumnTextAlign.CENTER);

    grid.addColumn(new NumberRenderer<>(InvoiceItem::getUnitPrice,
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

    grid.addCellFocusListener(event ->
            event.getItem().ifPresent(item -> grid.asSingleSelect().setValue(item)));
    Shortcuts.addShortcutListener(grid,
            e -> {
              var selected = grid.asSingleSelect().getValue();
              setDetail(createDetailContent(selected));
            },
            Key.ENTER);
  }

  private VerticalLayout createDetailContent(InvoiceItem item) {
    var content = new VerticalLayout();
    content.setSizeFull();
    content.setPadding(true);
    content.setSpacing(true);

    // Header with close button and actions
    var header = createDetailHeader(item);
    content.add(header);

    // Invoice item information cards
    content.add(createBasicInfoCard(item));
    content.add(createTaxInfoCard(item));

    return content;
  }

  private HorizontalLayout createDetailHeader(InvoiceItem item) {
    var header = new HorizontalLayout();
    header.setWidthFull();
    header.setAlignItems(HorizontalLayout.Alignment.CENTER);
    header.setSpacing(true);
    header.getStyle()
            .set("margin-bottom", "var(--lumo-space-m)");

    var itemName = new H2(nullSafe(item.getName()));
    itemName.getStyle()
            .set("margin", "0")
            .set("flex", "1");

    var editBtn = new Button("Edit", new Icon(VaadinIcon.EDIT), e -> {
      var dialog = new InvoiceItemDialog(invoiceItemService, i -> {
        updateInvoiceItem(i);
        Notification.show("Invoice item updated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
      });
      dialog.openUpdateForm(item);
    });
    editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

    var deleteBtn = new Button("Delete", new Icon(VaadinIcon.TRASH), e -> {
      var confirmDialog = new ConfirmDialog();
      confirmDialog.setHeader("Delete Invoice Item?");
      confirmDialog.setText("Are you sure you want to delete '" + item.getName() + "' ?");
      confirmDialog.setCancelable(true);
      confirmDialog.setConfirmText("Delete");
      confirmDialog.setConfirmButtonTheme("error primary");
      confirmDialog.addConfirmListener(confirmEvent -> deleteInvoiceItem(item));
      confirmDialog.open();
    });
    deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

    header.add(itemName, editBtn, deleteBtn);
    return header;
  }

  private Card createBasicInfoCard(InvoiceItem item) {
    Card card = new Card();
    card.setWidthFull();

    H3 title = new H3("Basic Information");
    title.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));

    card.add(
            title,
            createInfoRow(VaadinIcon.PACKAGE, "Item Name", nullSafe(item.getName())),
            createInfoRow(VaadinIcon.FILE_TEXT, "Description",
                    item.getDescription() != null ? item.getDescription() : "N/A"),
            createInfoRow(VaadinIcon.BARCODE, "HSN Code", nullSafe(item.getHsnCode())),
            createInfoRow(VaadinIcon.SCALE, "Unit",
                    item.getUnit() != null ? item.getUnit().toString() : "N/A"),
            createInfoRow(VaadinIcon.TAG, "Type",
                    item.getType() != null ? item.getType().toString() : "N/A"),
            createInfoRow(VaadinIcon.MONEY, "Unit Price",
                    item.getUnitPrice() != null ? currencyFormat.format(item.getUnitPrice()) : "₹0.00"),
            createToggleableInfoRow(VaadinIcon.MONEY_WITHDRAW, "Cost Price",
                    item.getCostPrice() != null ? currencyFormat.format(item.getCostPrice()) : "₹0.00"),
            createToggleableInfoRow(VaadinIcon.USER, "Vendor Name",
                    item.getVendorName() != null ? item.getVendorName() : "N/A")
    );

    return card;
  }

  private Card createTaxInfoCard(InvoiceItem item) {
    Card card = new Card();
    card.setWidthFull();

    H3 title = new H3("Tax Information");
    title.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)")
            .set("font-size", "var(--lumo-font-size-l)")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

    card.add(
            title,
            createInfoRow(VaadinIcon.PIGGY_BANK, "GST Rate",
                    item.getGst() != null ? item.getGst() + "%" : "0%")
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

  private HorizontalLayout createToggleableInfoRow(VaadinIcon iconType, String label, String value) {
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

    // Create masked value initially
    var valueSpan = new Span("•••••");
    valueSpan.getStyle()
            .set("color", "var(--lumo-body-text-color)")
            .set("word-break", "break-word")
            .set("flex", "1")
            .set("font-family", "monospace");

    // Create toggle button with eye icon
    var toggleBtn = new Button(new Icon(VaadinIcon.EYE_SLASH));
    toggleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
    toggleBtn.getStyle()
            .set("margin-left", "var(--lumo-space-xs)")
            .set("min-width", "auto")
            .set("padding", "4px");
    toggleBtn.setTooltipText("Show " + label.toLowerCase());

    // Toggle functionality
    var isVisible = new boolean[]{false};
    toggleBtn.addClickListener(e -> {
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

  private void addInvoiceItem(InvoiceItem item) {
    if (!dataProvider.getItems().contains(item)) {
      dataProvider.getItems().add(item);
      dataProvider.refreshAll();
    }
  }

  private void updateInvoiceItem(InvoiceItem updated) {
    dataProvider.refreshAll();
    setDetail(createDetailContent(updated));
  }

  private void deleteInvoiceItem(InvoiceItem item) {
    try {
      invoiceItemService.deleteById(item.getId());

      Notification.show("Invoice item deleted successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

      dataProvider.getItems().remove(item);
      dataProvider.refreshAll();
      closeDetail();
    } catch (Exception ex) {
      var notification = Notification.show("Failed to delete invoice item: " + ex.getMessage());
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
