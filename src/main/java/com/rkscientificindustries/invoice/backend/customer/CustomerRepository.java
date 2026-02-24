package com.rkscientificindustries.invoice.backend.customer;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends ListCrudRepository<Customer, Long> {
  List<Customer> findAllByOrderByIdAsc();

  Optional<Customer> findByGstin(String gstin);
}
