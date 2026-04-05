package com.rkscientificindustries.invoice.backend.product;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.List;

public interface ProductRepository extends ListCrudRepository<Product, Long>, ListPagingAndSortingRepository<Product, Long> {
  List<Product> findAllByOrderByIdAsc();
}
