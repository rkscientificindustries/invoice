package com.rkscientificindustries.invoice.backend.product;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ProductRepository extends ListCrudRepository<Product, Long> {
  List<Product> findAllByOrderByIdAsc();
}
