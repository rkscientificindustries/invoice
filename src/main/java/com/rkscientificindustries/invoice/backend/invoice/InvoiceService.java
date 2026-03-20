package com.rkscientificindustries.invoice.backend.invoice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {
  private final InvoiceRepository invoiceRepository;

  public InvoiceService(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  public List<Invoice> findAll() {
    return invoiceRepository.findAllByOrderByIdAsc();
  }

  public Optional<Invoice> findById(Long id) {
    return invoiceRepository.findById(id);
  }

  @Transactional
  public Invoice save(Invoice invoice) {
    if (invoice.getStatus() != InvoiceStatus.DRAFT && invoice.getItems().isEmpty()) {
      throw new IllegalStateException("Invoice must have at least one item before finalizing.");
    }
    return invoiceRepository.save(invoice);
  }

  @Transactional
  public void deleteById(Long id) {
    invoiceRepository.deleteById(id);
  }
}
