package com.rkscientificindustries.invoice.backend.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private ProductService productService;

  @Test
  @DisplayName("Should save product and return persisted entity")
  void shouldSaveProduct() {
    Product product = Product.of(
        "Copper Wire",
        "Industrial copper wire",
        "7408",
        Product.Unit.PCS,
        new BigDecimal("100.00"),
        new BigDecimal("75.00"),
        Product.ItemType.BO,
        new BigDecimal("18.00"),
        "RK Vendor"
    );
    product.setId(1L);

    when(productRepository.save(any(Product.class))).thenReturn(product);

    Product saved = productService.save(product);

    assertThat(saved.getId()).isEqualTo(1L);
    verify(productRepository).save(product);
  }

  @Test
  @DisplayName("Should return all products ordered by id")
  void shouldFindAllProducts() {
    Product product = Product.of(
        "Flux",
        "Soldering flux",
        "3810",
        Product.Unit.PCS,
        new BigDecimal("50.00"),
        new BigDecimal("30.00"),
        Product.ItemType.BO,
        new BigDecimal("5.00"),
        "RK Vendor"
    );
    product.setId(5L);

    when(productRepository.findAllByOrderByIdAsc()).thenReturn(List.of(product));

    List<Product> products = productService.findAll();

    assertThat(products).hasSize(1);
    assertThat(products.getFirst().getId()).isEqualTo(5L);
    verify(productRepository).findAllByOrderByIdAsc();
  }

  @Test
  @DisplayName("Should find product by id")
  void shouldFindById() {
    Product product = new Product();
    product.setId(42L);
    when(productRepository.findById(42L)).thenReturn(Optional.of(product));

    Optional<Product> found = productService.findById(42L);

    assertThat(found).isPresent().contains(product);
    verify(productRepository).findById(42L);
  }

  @Test
  @DisplayName("Should delegate deletion by id")
  void shouldDeleteById() {
    productService.deleteById(7L);

    verify(productRepository).deleteById(7L);
  }
}
