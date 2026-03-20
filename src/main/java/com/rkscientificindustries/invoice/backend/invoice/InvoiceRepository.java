package com.rkscientificindustries.invoice.backend.invoice;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface InvoiceRepository extends ListCrudRepository<Invoice, Long> {
  List<Invoice> findAllByOrderByIdAsc();

  @Query("SELECT * FROM line_items WHERE invoice_id = :invoiceId ORDER BY line_order")
  List<LineItem> findLineItemsByInvoiceId(Long invoiceId);
}
