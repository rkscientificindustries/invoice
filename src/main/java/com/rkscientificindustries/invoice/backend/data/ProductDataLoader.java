package com.rkscientificindustries.invoice.backend.data;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.product.Product;
import com.rkscientificindustries.invoice.backend.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Slf4j
@Order(2)
@Profile("demo")
@Component
public class ProductDataLoader implements DataLoader {
  private final ProductRepository productRepository;
  private final InvoiceProperties invoiceProperties;
  private final Random random = new Random();

  public ProductDataLoader(ProductRepository productRepository, InvoiceProperties invoiceProperties) {
    this.productRepository = productRepository;
    this.invoiceProperties = invoiceProperties;
  }

  @Override
  public void load() {
    int numberOfProducts = invoiceProperties.getProducts();
    for (int i = 0; i < numberOfProducts; i++) {
      var product = generateMockProduct(i);
      productRepository.save(product);
    }
    log.info("\uD83D\uDCE6 Successfully loaded {} mock products", numberOfProducts);
  }

  private Product generateMockProduct(int index) {
    var name = MockDataConstants.PRODUCT_NAMES[index % MockDataConstants.PRODUCT_NAMES.length];
    if (index >= MockDataConstants.PRODUCT_NAMES.length) {
      name += " (" + (index / MockDataConstants.PRODUCT_NAMES.length + 1) + ")";
    }

    var description = MockDataConstants.PRODUCT_DESCRIPTIONS[index % MockDataConstants.PRODUCT_DESCRIPTIONS.length];
    var hsnCode = MockDataConstants.HSN_CODES[index % MockDataConstants.HSN_CODES.length];
    var unit = Product.Unit.values()[index % Product.Unit.values().length];
    var type = Product.ItemType.values()[index % Product.ItemType.values().length];

    // Only 5% or 18% GST rates allowed
    var gstRate = (index % 2 == 0) ? BigDecimal.valueOf(5) : BigDecimal.valueOf(18);

    // Generate unit price between ₹50 and ₹5000 with 2 decimal places
    var randomPrice = 50 + (random.nextDouble() * 4950);
    var unitPrice = BigDecimal.valueOf(randomPrice).setScale(2, RoundingMode.HALF_UP);

    // Generate cost price (70-90% of unit price)
    var costMultiplier = 0.70 + (random.nextDouble() * 0.20);
    var costPrice = unitPrice.multiply(BigDecimal.valueOf(costMultiplier))
        .setScale(2, RoundingMode.HALF_UP);

    var vendorName = MockDataConstants.VENDOR_NAMES[index % MockDataConstants.VENDOR_NAMES.length];

    return Product.of(name, description, hsnCode, unit, unitPrice, costPrice, type, gstRate, vendorName);
  }
}
