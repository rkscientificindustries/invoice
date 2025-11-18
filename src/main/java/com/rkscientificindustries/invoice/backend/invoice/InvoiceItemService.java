package com.rkscientificindustries.invoice.backend.invoice;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Validated
@Transactional
public class InvoiceItemService {
  private static final Logger logger = LoggerFactory.getLogger(InvoiceItemService.class);
  private final InvoiceItemRepository invoiceItemRepository;

  public InvoiceItemService(InvoiceItemRepository invoiceItemRepository) {
    this.invoiceItemRepository = invoiceItemRepository;
  }

  public InvoiceItem save(@Valid InvoiceItem item) {
    logger.debug("Saving invoice item: {}", item);
    // Calculate line total before saving: quantity × unit price
    if (item.getQuantity() != null && item.getUnitPrice() != null) {
      BigDecimal lineTotal = item.getQuantity()
              .multiply(item.getUnitPrice())
              .setScale(2, RoundingMode.HALF_UP);
      item.setLineTotal(lineTotal);
      logger.debug("Calculated line total {} for item {}", lineTotal, item.getName());
    }
    InvoiceItem saved = invoiceItemRepository.save(item);
    logger.info("Saved InvoiceItem id={}", saved.getId());
    return saved;
  }

  @Transactional(readOnly = true)
  public List<InvoiceItem> findAll() {
    logger.debug("Fetching all invoice items");
    List<InvoiceItem> items = invoiceItemRepository.findAllByOrderByIdAsc();
    logger.debug("Found {} invoice items", items.size());
    return items;
  }

  public void deleteById(Long id) {
    logger.info("Deleting InvoiceItem id={}", id);
    invoiceItemRepository.deleteById(id);
  }
}
