package com.rkscientificindustries.invoice.backend.invoice;

import com.rkscientificindustries.invoice.TestcontainersConfiguration;
import com.rkscientificindustries.invoice.backend.config.DataConfig;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerRepository;
import com.rkscientificindustries.invoice.backend.customer.CustomerType;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductRepository;
import com.rkscientificindustries.invoice.backend.utils.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, DataConfig.class})
class InvoiceRepositoryTest {
  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private ProductRepository productRepository;

  private Long customerId;
  private Long productId;

  @BeforeEach
  void setUp() {
    invoiceRepository.deleteAll();
    productRepository.deleteAll();
    customerRepository.deleteAll();

    Customer customer = Customer.of(
        "Acme Corp", "contact@acme.com", "1234567890", CustomerType.BUSINESS,
        "22AAAAA0000A1Z5", "123 Business Rd", "Metropolis", State.MAHARASHTRA, "400001"
    );
    customerId = customerRepository.save(customer).getId();

    Product product = Product.of(
        "Product A", "Description A", "1234", Product.Unit.PCS,
        new BigDecimal("100.00"), new BigDecimal("50.00"),
        Product.ItemType.BO, new BigDecimal("18.00"), "Vendor X"
    );
    productId = productRepository.save(product).getId();
  }

  @AfterEach
  void tearDown() {
    invoiceRepository.deleteAll();
    productRepository.deleteAll();
    customerRepository.deleteAll();
  }

  @Test
  void shouldSaveAndFindInvoiceWithLineItems() {
    // Arrange
    LineItem item = LineItem.builder()
        .productId(productId)
        .quantity(2)
        .unitPrice(new BigDecimal("100.00"))
        .gstRate(new BigDecimal("18.00"))
        .taxAmount(new BigDecimal("36.00"))
        .totalAmount(new BigDecimal("236.00"))
        .build();

    List<LineItem> items = new ArrayList<>();
    items.add(item);

    Invoice invoice = Invoice.builder()
        .invoiceDate(LocalDate.now())
        .billedTo(customerId)
        .shippedTo(customerId)
        .place(State.MAHARASHTRA)
        .transport(Invoice.Transport.COURIER)
        .courierName("FastShip")
        .packageCount(1)
        .items(items)
        .subtotal(new BigDecimal("200.00"))
        .totalTax(new BigDecimal("36.00"))
        .totalAmount(new BigDecimal("236.00"))
        .status(InvoiceStatus.DRAFT)
        .build();

    // Act
    Invoice savedInvoice = invoiceRepository.save(invoice);
    Optional<Invoice> foundInvoice = invoiceRepository.findById(savedInvoice.getId());

    // Assert
    assertThat(foundInvoice).isPresent();
    assertThat(foundInvoice.get().getInvoiceNumber()).isNotNull();
    assertThat(foundInvoice.get().getItems()).hasSize(1);
    assertThat(foundInvoice.get().getItems().getFirst().getProductId()).isEqualTo(productId);
  }
}
