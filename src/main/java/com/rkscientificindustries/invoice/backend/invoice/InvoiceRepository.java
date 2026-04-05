package com.rkscientificindustries.invoice.backend.invoice;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends ListCrudRepository<Invoice, Long> {
  List<Invoice> findAllByOrderByIdAsc();

  @Query("SELECT COALESCE(SUM(total_amount), 0) FROM invoices")
  BigDecimal sumTotalAmount();

  @Query("SELECT COALESCE(AVG(total_amount), 0) FROM invoices")
  BigDecimal averageTotalAmount();

  @Query("""
      SELECT COALESCE(SUM(total_amount), 0)
      FROM invoices
      WHERE invoice_date >= :startDate
        AND invoice_date < :endDate
      """)
  BigDecimal sumTotalAmountBetweenDates(LocalDate startDate, LocalDate endDate);

  @Query("SELECT * FROM invoices ORDER BY created_date DESC, id DESC LIMIT 5")
  List<Invoice> findLatestFiveByCreatedDate();
}
