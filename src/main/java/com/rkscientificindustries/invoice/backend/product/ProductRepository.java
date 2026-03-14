package com.rkscientificindustries.invoice.backend.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ProductRepository extends ListCrudRepository<Product, Long>, ListPagingAndSortingRepository<Product, Long> {
  List<Product> findAllByOrderByIdAsc();
  List<Product> findByNameContainingIgnoreCaseOrderByName(String name, Pageable pageable);
  int countByNameContainingIgnoreCase(String name);
}
