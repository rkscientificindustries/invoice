package com.rkscientificindustries.invoice.ui.invoices;

import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.pdf.InvoicePdfService;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.ui.utils.AppConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import org.jspecify.annotations.NonNull;

import com.vaadin.flow.component.select.Select;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

@StyleSheet("invoice-preview-dialog.css")
public class InvoicePreviewDialog extends Dialog {
  private static final String PDF_CONTENT_TYPE = "application/pdf";

  private InvoicePreviewPayloadProvider.PdfPreviewPayload payload;
  
  private final InvoicePreviewPayloadProvider payloadProvider;
  private final Invoice invoice;
  private final Customer billedCustomer;
  private final Customer shippedCustomer;
  private final List<Product> products;
  private final String termsAndConditions;
  
  private IFrame frame;

  public InvoicePreviewDialog(Invoice invoice, Customer billedCustomer, @NonNull Customer shippedCustomer,
                              List<Product> products, InvoicePdfService pdfService,
                              @NonNull String termsAndConditions) {
    this.invoice = invoice;
    this.billedCustomer = billedCustomer;
    this.shippedCustomer = shippedCustomer;
    this.products = products;
    this.termsAndConditions = termsAndConditions.isBlank() ? AppConstants.DEFAULT_TERMS : termsAndConditions;
    
    this.payloadProvider = new InvoicePreviewPayloadProvider(pdfService);
    this.payload = payloadProvider.createPayload(invoice, billedCustomer, shippedCustomer, products, this.termsAndConditions, "Original Copy");

    setWidth("800px");
    setMaxHeight("90vh");
    setResizable(true);
    setDraggable(true);
    getElement().getThemeList().add("no-padding");

    add(buildHeader(), buildPreviewContent());
  }

  // ── Dialog header ──────────────────────────────────────────────────
  private HorizontalLayout buildHeader() {
    var title = new H3("Final PDF Preview");
    title.addClassName("preview-dialog-title");
    
    Select<String> copySelect = new Select<>();
    copySelect.setItems("Original Copy", "Duplicate Copy", "Triplicate Copy");
    copySelect.setValue("Original Copy");
    copySelect.addValueChangeListener(event -> {
        this.payload = payloadProvider.createPayload(invoice, billedCustomer, shippedCustomer, products, termsAndConditions, event.getValue());
        updateIFrameSource();
    });

    var downloadAnchor = getDownloadAnchor();
    downloadAnchor.addClassName("preview-hidden-anchor");

    var downloadBtn = new Button("Download", VaadinIcon.DOWNLOAD.create(),
        _ -> downloadAnchor.getElement().callJsFunction("click")
    );
    downloadBtn.addThemeVariants(ButtonVariant.PRIMARY);
    downloadBtn.setId("download-pdf-btn");

    var closeBtn = new Button(VaadinIcon.CLOSE.create(), _ -> close());
    closeBtn.addThemeVariants(ButtonVariant.TERTIARY);

    var actions = new HorizontalLayout(copySelect, downloadBtn, closeBtn);
    actions.setSpacing(true);
    actions.setAlignItems(FlexComponent.Alignment.CENTER);
    actions.addClassName("preview-dialog-actions");
    actions.add(downloadAnchor);

    var header = new HorizontalLayout(title, actions);
    header.addClassName("preview-dialog-header");
    header.setWidthFull();
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    return header;
  }

  private void updateIFrameSource() {
    if (frame != null && payload.pdfBytes().length > 0) {
      String encodedPdf = Base64.getEncoder().encodeToString(payload.pdfBytes());
      frame.setSrc("data:" + PDF_CONTENT_TYPE + ";base64," + encodedPdf);
    }
  }

  // ── Invoice preview (mimics A4) ───────────────────────────────────
  private VerticalLayout buildPreviewContent() {
    var content = new VerticalLayout();
    content.addClassName("invoice-preview-dialog");
    content.setSpacing(false);
    content.setPadding(false);
    content.getStyle().set("padding-top", "0");
    content.setAlignItems(FlexComponent.Alignment.CENTER);

    if (payload.pdfBytes().length == 0) {
      var fallback = new Div();
      fallback.setText("Unable to render preview. You can still download the final PDF.");
      fallback.addClassName("invoice-preview-paper");
      fallback.getStyle().set("padding", "1rem");
      content.add(fallback);
      return content;
    }

    String encodedPdf = Base64.getEncoder().encodeToString(payload.pdfBytes());
    this.frame = new IFrame("data:" + PDF_CONTENT_TYPE + ";base64," + encodedPdf);
    frame.setWidthFull();
    frame.setHeight("77vh");
    frame.getElement().setAttribute("title", "Final invoice PDF preview");
    frame.getStyle().set("border", "none");

    var container = new Div(frame);
    container.addClassName("invoice-preview-paper");
    container.setWidthFull();
    container.getStyle().set("padding", "0");

    content.add(container);
    return content;
  }

  private Anchor getDownloadAnchor() {
    var downloadAnchor = new Anchor(
        DownloadHandler.fromInputStream(_ -> {
          try {
            return new DownloadResponse(
                new ByteArrayInputStream(payload.pdfBytes()),
                payload.filename(),
                PDF_CONTENT_TYPE,
                payload.pdfBytes().length
            );
          } catch (Exception e) {
            return DownloadResponse.error(500);
          }
        }), ""
    );
    downloadAnchor.setId("download-pdf-anchor");
    return downloadAnchor;
  }
}
