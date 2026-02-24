package com.rkscientificindustries.invoice.backend.customer;

import com.rkscientificindustries.invoice.TestcontainersConfiguration;
import com.rkscientificindustries.invoice.backend.config.DataConfig;
import com.rkscientificindustries.invoice.backend.utils.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import({TestcontainersConfiguration.class, DataConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {
  @Autowired
  private CustomerRepository customerRepository;

  @BeforeEach
  void setUp() {
    customerRepository.deleteAll();
  }

  @AfterEach
  void tearDown() {
    customerRepository.deleteAll();
  }

  @Test
  void shouldFindCustomerByGstin() {
    // Arrange
    Customer customer = Customer.of(
        "Acme Corp",
        "contact@acme.com",
        "1234567890",
        CustomerType.BUSINESS,
        "22AAAAA0000A1Z5",
        "123 Business Rd",
        "Metropolis",
        State.MAHARASHTRA,
        "400001"
    );
    customerRepository.save(customer);

    // Act
    Optional<Customer> found = customerRepository.findByGstin("22AAAAA0000A1Z5");

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Acme Corp");
    assertThat(found.get().getGstin()).isEqualTo("22AAAAA0000A1Z5");
  }

  @Test
  void shouldReturnEmptyWhenGstinNotFound() {
    // Act
    Optional<Customer> found = customerRepository.findByGstin("99ZZZZZ9999Z9Z9");

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void shouldFindAllByOrderByIdAsc() {
    // Arrange
    Customer customer1 = Customer.of(
        "First Corp",
        "first@test.com",
        "1111111111",
        CustomerType.BUSINESS,
        "22BBBBB0000A1Z5",
        "First St",
        "City1",
        State.GUJARAT,
        "380001"
    );
    Customer customer2 = Customer.of(
        "Second Corp",
        "second@test.com",
        "2222222222",
        CustomerType.BUSINESS,
        "22CCCCC0000A1Z5",
        "Second St",
        "City2",
        State.DELHI,
        "110001"
    );

    // Save entities
    customerRepository.save(customer1);
    customerRepository.save(customer2);

    // Act
    List<Customer> customers = customerRepository.findAllByOrderByIdAsc();

    // Assert
    assertThat(customers).hasSize(2);
    // Assert ordering by ID
    assertThat(customers.get(0).getId()).isLessThan(customers.get(1).getId());
  }
}
