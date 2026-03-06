package com.rkscientificindustries.invoice.backend.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock
  private CustomerRepository customerRepository;

  @InjectMocks
  private CustomerService customerService;

  private Customer testCustomer;

  @BeforeEach
  void setUp() {
    testCustomer = new Customer();
    testCustomer.setId(1L);
    testCustomer.setName("Test Company");
    testCustomer.setGstin("29ABCDE1234F1Z5");
  }

  @Test
  @DisplayName("Should return saved customer when valid customer is saved")
  void shouldSaveValidCustomer() {
    when(customerRepository.findByGstin(testCustomer.getGstin())).thenReturn(Optional.empty());
    when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

    var saved = customerService.save(testCustomer);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(1L);
    verify(customerRepository).findByGstin(testCustomer.getGstin());
    verify(customerRepository).save(testCustomer);
  }

  @Test
  @DisplayName("Should throw exception when saving customer with existing GSTIN")
  void shouldFailOnDuplicateGstin() {
    var existingCustomer = new Customer();
    existingCustomer.setId(2L);
    existingCustomer.setGstin("29ABCDE1234F1Z5");

    when(customerRepository.findByGstin(testCustomer.getGstin())).thenReturn(Optional.of(existingCustomer));

    assertThatThrownBy(() -> customerService.save(testCustomer))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Customer with GSTIN 29ABCDE1234F1Z5 already exists");

    verify(customerRepository).findByGstin(testCustomer.getGstin());
  }

  @Test
  @DisplayName("Should save successfully when saving customer with existing GSTIN for same customer")
  void shouldUpdateExistingCustomer() {
    when(customerRepository.findByGstin(testCustomer.getGstin())).thenReturn(Optional.of(testCustomer));
    when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

    var saved = customerService.save(testCustomer);

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo(1L);
    verify(customerRepository).save(testCustomer);
  }

  @Test
  @DisplayName("Should return customer when finding by existing ID")
  void shouldFindCustomerById() {
    when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

    Optional<Customer> result = customerService.findById(1L);

    assertThat(result).isPresent().contains(testCustomer);
    verify(customerRepository).findById(1L);
  }

  @Test
  @DisplayName("Should return all customers ordered by ID when finding all")
  void shouldFindAllCustomers() {
    List<Customer> customers = List.of(testCustomer);
    when(customerRepository.findAllByOrderByIdAsc()).thenReturn(customers);

    List<Customer> result = customerService.findAll();

    assertThat(result).hasSize(1).contains(testCustomer);
    verify(customerRepository).findAllByOrderByIdAsc();
  }

  @Test
  @DisplayName("Should delegate to repository when deleting by valid ID")
  void shouldDeleteCustomerById() {
    customerService.deleteById(1L);

    verify(customerRepository).deleteById(1L);
  }
}