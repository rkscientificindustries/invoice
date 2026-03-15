package com.rkscientificindustries.invoice.backend.customer;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Slf4j
@Validated
@Transactional
@Service
public class CustomerService {
  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Customer save(@Valid Customer customer) {
    log.debug("Saving customer: {}", customer);
    if (customer.getGstin() != null) {
      Optional<Customer> existing = customerRepository.findByGstin(customer.getGstin());
      if (existing.isPresent() && !existing.get().getId().equals(customer.getId())) {
        log.warn("Attempt to save duplicate GSTIN {} for customer id={}", customer.getGstin(), customer.getId());
        throw new IllegalArgumentException("Customer with GSTIN " + customer.getGstin() + " already exists");
      }
    }
    Customer saved = customerRepository.save(customer);
    log.info("Saved Customer id={}", saved.getId());
    return saved;
  }

  public Optional<Customer> findById(Long id) {
    return customerRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public List<Customer> findAll() {
    log.debug("Fetching all customers");
    List<Customer> customers = customerRepository.findAllByOrderByIdAsc();
    log.debug("Found {} customers", customers.size());
    return customers;
  }

  public void deleteById(Long id) {
    log.info("Deleting customer id={}", id);
    customerRepository.deleteById(id);
  }
}
