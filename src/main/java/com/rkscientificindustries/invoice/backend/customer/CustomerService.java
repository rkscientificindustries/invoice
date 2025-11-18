package com.rkscientificindustries.invoice.backend.customer;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional
public class CustomerService {
  private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Customer save(@Valid Customer customer) {
    logger.debug("Saving customer: {}", customer);
    if (customer.getGstin() != null) {
      Optional<Customer> existing = customerRepository.findByGstin(customer.getGstin());
      if (existing.isPresent() && !existing.get().getId().equals(customer.getId())) {
        logger.warn("Attempt to save duplicate GSTIN {} for customer id={}", customer.getGstin(), customer.getId());
        throw new IllegalArgumentException("Customer with GSTIN " + customer.getGstin() + " already exists");
      }
    }
    Customer saved = customerRepository.save(customer);
    logger.info("Saved Customer id={}", saved.getId());
    return saved;
  }

  @Transactional(readOnly = true)
  public List<Customer> findAll() {
    logger.debug("Fetching all customers");
    List<Customer> customers = customerRepository.findAllByOrderByIdAsc();
    logger.debug("Found {} customers", customers.size());
    return customers;
  }

  public void deleteById(Long id) {
    logger.info("Deleting customer id={}", id);
    customerRepository.deleteById(id);
  }
}
