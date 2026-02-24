package com.rkscientificindustries.invoice.backend.product;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
public class ProductService {
  private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Product save(@Valid Product product) {
    logger.debug("Saving product: {}", product);
    var savedProduct = productRepository.save(product);
    logger.info("Saved Product id={}", savedProduct.getId());
    return savedProduct;
  }

  @Transactional(readOnly = true)
  public List<Product> findAll() {
    logger.debug("Fetching all products");
    var products = productRepository.findAllByOrderByIdAsc();
    logger.debug("Found {} products", products.size());
    return products;
  }

  public void deleteById(Long id) {
    logger.info("Deleting Product id={}", id);
    productRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public Optional<Product> findById(Long id) {
    return productRepository.findById(id);
  }
}
