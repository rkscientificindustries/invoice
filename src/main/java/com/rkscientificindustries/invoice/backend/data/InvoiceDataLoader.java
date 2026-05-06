package com.rkscientificindustries.invoice.backend.data;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerRepository;
import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceRepository;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceStatus;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductRepository;
import com.rkscientificindustries.invoice.backend.utils.State;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Order(3)
@Profile("demo")
@Component
public class InvoiceDataLoader implements DataLoader {
  private final InvoiceRepository invoiceRepository;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final InvoiceProperties invoiceProperties;
  private final Random random = new Random();

  public InvoiceDataLoader(InvoiceRepository invoiceRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository,
                           InvoiceProperties invoiceProperties) {
    this.invoiceRepository = invoiceRepository;
    this.customerRepository = customerRepository;
    this.productRepository = productRepository;
    this.invoiceProperties = invoiceProperties;
  }

  @Override
  public void load() {
    // Fetch all available customers and products
    List<Customer> customers = customerRepository.findAll();
    List<Product> products = productRepository.findAll();

    if (customers.isEmpty() || products.isEmpty()) {
      log.warn("Skipping invoice generation: customers={}, products={}", customers.size(), products.size());
      return;
    }

    int numberOfInvoices = invoiceProperties.getInvoices();
    for (int i = 0; i < numberOfInvoices; i++) {
      try {
        var invoice = generateInvoice(i, customers, products);
        invoiceRepository.save(invoice);
      } catch (Exception e) {
        log.error("Error generating invoice at index {}", i, e);
      }
    }
    log.info("\uD83D\uDCC3 Successfully loaded {} mock invoices", numberOfInvoices);
  }

  private Invoice generateInvoice(int index, List<Customer> customers, List<Product> products) {
    // Pick random customer
    Customer billedCustomer = customers.get(random.nextInt(customers.size()));
    Customer shippedCustomer = customers.get(random.nextInt(customers.size()));

    // Generate invoice date within last 90 days
    LocalDate invoiceDate = LocalDate.now().minusDays(random.nextInt(90));

    // Pick random place of supply (state)
    State placeOfSupply = State.values()[random.nextInt(State.values().length)];

    // Generate transport info
    String courierName = "Transport Co " + (index % 10 + 1);
    String vehicleNumber = String.format("TN%02dAB%04d", index % 99, random.nextInt(10000));
    String eWayBillNumber = String.format("%015d", 100000000000000L + random.nextInt(900000000));

    // Generate 2-5 line items
    int lineItemCount = 2 + random.nextInt(4);
    List<LineItem> invoiceLines = new ArrayList<>();

    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;

    for (int lineOrder = 0; lineOrder < lineItemCount; lineOrder++) {
      var product = products.get(random.nextInt(products.size()));
      int quantity = 1 + random.nextInt(10);

      BigDecimal lineSubtotal = product.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
      BigDecimal lineTax = lineSubtotal.multiply(product.getGstRate())
          .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      BigDecimal lineTotal = lineSubtotal.add(lineTax);

      var lineItem = LineItem.builder()
          .productId(product.getId())
          .quantity(quantity)
          .unitPrice(product.getUnitPrice())
          .gstRate(product.getGstRate())
          .taxAmount(lineTax)
          .totalAmount(lineTotal)
          .build();

      invoiceLines.add(lineItem);
      subtotal = subtotal.add(lineSubtotal);
      totalTax = totalTax.add(lineTax);
    }

    // Apply random discount (0-10%)
    BigDecimal discountPercentage = BigDecimal.valueOf(random.nextInt(11));
    BigDecimal discountAmount = subtotal.multiply(discountPercentage)
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);

    // Calculate total amount (subtotal after discount + tax)
    BigDecimal totalAmount = discountedSubtotal.add(totalTax);

    // Build and return invoice
    return Invoice.builder()
        .invoiceDate(invoiceDate)
        .billedTo(billedCustomer.getId())
        .shippedTo(shippedCustomer.getId())
        .place(placeOfSupply)
        .transport(getRandomTransport())
        .courierName(courierName)
        .vehicleNumber(vehicleNumber)
        .eWayBillNumber(eWayBillNumber)
        .packageCount(1 + random.nextInt(5))
        .items(invoiceLines)
        .subtotal(subtotal)
        .discountPercentage(discountPercentage)
        .totalTax(totalTax)
        .totalAmount(totalAmount)
        .status(InvoiceStatus.DRAFT)
        .build();
  }

  private Invoice.Transport getRandomTransport() {
    return Invoice.Transport.values()[random.nextInt(Invoice.Transport.values().length)];
  }
}
