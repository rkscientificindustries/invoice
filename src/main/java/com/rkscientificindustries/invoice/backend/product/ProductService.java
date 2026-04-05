package com.rkscientificindustries.invoice.backend.product;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@Transactional
@Service
public class ProductService {
  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Product save(@Valid Product product) {
    log.debug("Saving product: {}", product);
    var savedProduct = productRepository.save(product);
    log.info("Saved Product id={}", savedProduct.getId());
    return savedProduct;
  }

  @Transactional(readOnly = true)
  public List<Product> findAll() {
    log.debug("Fetching all products");
    var products = productRepository.findAllByOrderByIdAsc();
    log.debug("Found {} products", products.size());
    return products;
  }

  public void deleteById(Long id) {
    log.info("Deleting Product id={}", id);
    productRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public Optional<Product> findById(Long id) {
    return productRepository.findById(id);
  }
}
