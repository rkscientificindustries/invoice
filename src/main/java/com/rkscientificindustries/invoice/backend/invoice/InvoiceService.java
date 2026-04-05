package com.rkscientificindustries.invoice.backend.invoice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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

  @Transactional(readOnly = true)
  public BigDecimal thisMonthInvoiceValue() {
    var startOfMonth = LocalDate.now().withDayOfMonth(1);
    var startOfNextMonth = startOfMonth.plusMonths(1);
    return invoiceRepository.sumTotalAmountBetweenDates(startOfMonth, startOfNextMonth);
  }

  @Transactional(readOnly = true)
  public BigDecimal totalInvoiceValue() {
    return invoiceRepository.sumTotalAmount();
  }

  @Transactional(readOnly = true)
  public BigDecimal averageInvoiceValue() {
    return invoiceRepository.averageTotalAmount();
  }

  @Transactional(readOnly = true)
  public List<Invoice> findRecentInvoices() {
    return invoiceRepository.findLatestFiveByCreatedDate();
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
}
