package com.rkscientificindustries.invoice.backend.invoice;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InvoiceItemRepository extends CrudRepository<InvoiceItem, Long> {
  List<InvoiceItem> findAllByOrderByIdAsc();
}
