package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerService;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceService;
import com.rkscientificindustries.invoice.ui.MainLayout;
import com.rkscientificindustries.invoice.ui.utils.AppConstants;
import com.rkscientificindustries.invoice.ui.utils.FabButton;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import com.vaadin.flow.component.html.Span;

@PageTitle("Invoices")
@Route(value = "invoices", layout = MainLayout.class)
public class InvoiceListView extends MasterDetailLayout {
  private final InvoiceService invoiceService;
  private final CustomerService customerService;
  private final Grid<Invoice> grid = new Grid<>(Invoice.class, false);
  private final ListDataProvider<Invoice> dataProvider;
  private final InvoiceEditor editor;
  private final Map<Invoice, Integer> rowIndexMap = new HashMap<>();

  public InvoiceListView(InvoiceService invoiceService, CustomerService customerService, InvoiceEditor editor) {
    this.invoiceService = invoiceService;
    this.customerService = customerService;
    this.editor = editor;

    setSizeFull();

    var masterContent = new VerticalLayout();
    masterContent.setSizeFull();
    masterContent.setPadding(false);

    grid.setSizeFull();
    configureGrid();
    masterContent.add(grid);
    masterContent.setFlexGrow(1, grid);

    var addInvoiceBtn = FabButton.create("Add Invoice", _ -> {
      var invoice = Invoice.builder()
          .invoiceDate(LocalDate.now())
          .transport(Invoice.Transport.COURIER)
          .packageCount(1)
          .build();
      openInvoiceEditor(invoice);
    });
    masterContent.add(addInvoiceBtn);

    setMaster(masterContent);

    editor.setOnSave(this::saveInvoice);
    editor.setOnCancel(this::closeDetail);

    setDetail(null);

    dataProvider = new ListDataProvider<>(invoiceService.findAll());
    updateRowIndices();
    grid.setDataProvider(dataProvider);

    setMasterMinSize(AppConstants.MASTER_MIN_WIDTH);
    setDetailSize(AppConstants.DETAIL_WIDTH_WIDE);

    addBackdropClickListener(_ -> closeDetail());
    addDetailEscapePressListener(_ -> closeDetail());
  }

  private void updateRowIndices() {
    rowIndexMap.clear();
    int index = 1;
    for (Invoice invoice : dataProvider.getItems()) {
      rowIndexMap.put(invoice, index++);
    }
  }

  private void configureGrid() {
    grid.addComponentColumn(invoice -> {
          var index = rowIndexMap.get(invoice);
          return new Span(index != null ? String.valueOf(index) : "");
        })
        .setHeader("#")
        .setFlexGrow(0)
        .setWidth(AppConstants.INDEX_COLUMN_WIDTH);

    grid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice #");
    grid.addColumn(Invoice::getInvoiceDate).setHeader("Date");
    grid.addColumn(invoice -> {
      if (invoice.getBilledTo() == null) return "-";
      return customerService.findById(invoice.getBilledTo())
          .map(Customer::getName)
          .orElse("Unknown");
    }).setHeader("Billed To");
    grid.addColumn(Invoice::getTotalAmount).setHeader("Total").setTextAlign(ColumnTextAlign.END);

    grid.addSelectionListener(selection -> selection
        .getFirstSelectedItem()
        .ifPresentOrElse(
            this::openInvoiceEditor,
            this::closeDetail
        ));
  }

  private void openInvoiceEditor(Invoice invoice) {
    editor.setInvoice(invoice);
    var scroller = new Scroller(editor);
    scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
    scroller.setSizeFull();
    setDetail(scroller);
  }

  private void saveInvoice(Invoice invoice) {
    var saved = invoiceService.save(invoice);
    dataProvider.getItems().removeIf(i -> i.getId() != null && i.getId().equals(saved.getId()));
    dataProvider.getItems().add(invoice);
    updateRowIndices();
    dataProvider.refreshAll();
    closeDetail();
    var notification = Notification.show("Invoice saved successfully");
    notification.addThemeVariants(NotificationVariant.SUCCESS);
    notification.setPosition(Notification.Position.BOTTOM_CENTER);
  }

  private void closeDetail() {
    setDetail(null);
    grid.asSingleSelect().clear();
  }
}
