package com.rkscientificindustries.invoice.backend.customer;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
  List<Customer> findAllByOrderByIdAsc();

  Optional<Customer> findByGstin(String gstin);
}
