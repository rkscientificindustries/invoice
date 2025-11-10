package com.rkscientificindustries.invoice.backend.customer;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
public class CustomerService {
  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Customer save(@Valid Customer customer) {
    if (customer.getGstin() != null) {
      Optional<Customer> existing = customerRepository.findByGstin(customer.getGstin());
      if (existing.isPresent() && !existing.get().getId().equals(customer.getId())) {
        throw new IllegalArgumentException("Customer with GSTIN " + customer.getGstin() + " already exists");
      }
    }
    return customerRepository.save(customer);
  }

  @Transactional(readOnly = true)
  public List<Customer> findAll() {
    return customerRepository.findAllByOrderByIdAsc();
  }

  public void deleteById(Long id) {
    customerRepository.deleteById(id);
  }
}
