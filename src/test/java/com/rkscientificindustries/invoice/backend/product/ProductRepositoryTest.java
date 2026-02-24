package com.rkscientificindustries.invoice.backend.product;

import com.rkscientificindustries.invoice.TestcontainersConfiguration;
import com.rkscientificindustries.invoice.backend.config.DataConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, DataConfig.class})
class ProductRepositoryTest {
  @Autowired
  private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    productRepository.deleteAll();
  }

  @AfterEach
  void tearDown() {
    productRepository.deleteAll();
  }

  @Test
  void shouldFindAllByOrderByIdAsc() {
    // Arrange
    Product product1 = Product.of(
        "Product A",
        "Description A",
        "1234",
        Product.Unit.PCS,
        new BigDecimal("100.00"),
        new BigDecimal("50.00"),
        Product.ItemType.BO,
        new BigDecimal("18.00"),
        "Vendor X"
    );
    Product product2 = Product.of(
        "Product B",
        "Description B",
        "5678",
        Product.Unit.KG,
        new BigDecimal("200.00"),
        new BigDecimal("150.00"),
        Product.ItemType.RM,
        new BigDecimal("12.00"),
        "Vendor Y"
    );

    productRepository.save(product1);
    productRepository.save(product2);

    // Act
    List<Product> products = productRepository.findAllByOrderByIdAsc();

    // Assert
    assertThat(products).hasSize(2);
    assertThat(products.getFirst().getId()).isLessThan(products.get(1).getId());
  }

  @Test
  void shouldSaveAndFindProduct() {
    // Arrange
    Product product = Product.of(
        "Product C",
        "Description C",
        "9012",
        Product.Unit.L,
        new BigDecimal("300.00"),
        new BigDecimal("250.00"),
        Product.ItemType.MA,
        new BigDecimal("5.00"),
        "Vendor Z"
    );
    Product savedProduct = productRepository.save(product);

    // Act
    Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

    // Assert
    assertThat(foundProduct).isPresent();
    assertThat(foundProduct.get().getName()).isEqualTo("Product C");
  }
}
