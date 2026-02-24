package com.rkscientificindustries.invoice.backend.data;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerRepository;
import com.rkscientificindustries.invoice.backend.customer.CustomerType;
import com.rkscientificindustries.invoice.backend.utils.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Order(1)
@Profile("demo")
public class CustomerDataLoader implements DataLoader {
  private static final Logger logger = LoggerFactory.getLogger(CustomerDataLoader.class);

  private final CustomerRepository customerRepository;
  private final InvoiceProperties invoiceProperties;
  private final Random random = new Random();

  public CustomerDataLoader(CustomerRepository customerRepository, InvoiceProperties invoiceProperties) {
    this.customerRepository = customerRepository;
    this.invoiceProperties = invoiceProperties;
  }

  @Override
  public void load() {
    int numberOfCustomers = invoiceProperties.getCustomers();
    for (int i = 0; i < numberOfCustomers; i++) {
      var customer = generateMockCustomer(i);
      customerRepository.save(customer);
    }
    logger.info("\uD83D\uDC68\u200D\uD83D\uDCBC Successfully loaded {} mock customers", numberOfCustomers);
  }

  private Customer generateMockCustomer(int index) {
    var type = random.nextBoolean() ? CustomerType.BUSINESS : CustomerType.INDIVIDUAL;
    var name = type == CustomerType.BUSINESS ?
        MockDataConstants.COMPANY_NAMES[index % MockDataConstants.COMPANY_NAMES.length] + " " + (index / MockDataConstants.COMPANY_NAMES.length + 1) :
        MockDataConstants.INDIVIDUAL_NAMES[index % MockDataConstants.INDIVIDUAL_NAMES.length] + " " + (index / MockDataConstants.INDIVIDUAL_NAMES.length + 1);

    var email = generateEmail(name);
    var phone = generatePhone();
    var gstin = generateGSTIN(index);
    var street = (index + 1) + ", " + MockDataConstants.STREETS[index % MockDataConstants.STREETS.length];
    var city = MockDataConstants.CITIES[index % MockDataConstants.CITIES.length];
    var state = getStateForCity(city);
    var postalCode = generatePostalCode();

    return Customer.of(name, email, phone, type, gstin, street, city, state, postalCode);
  }

  private String generateEmail(String name) {
    var emailName = name.toLowerCase()
        .replaceAll("[^a-z0-9\\s]", "")
        .replaceAll("\\s+", ".");
    return emailName + "@example.com";
  }

  private String generatePhone() {
    return String.format("+91%010d", 6000000000L + random.nextInt(1000000000));
  }

  private String generateGSTIN(int index) {
    // Format: 2 digits state code + 5 letters PAN + 4 digits + 1 letter + 1 alphanumeric + Z + 1 alphanumeric
    int stateCode = (index % 37) + 1;
    String pan = String.format("ABCDE%04d", 1000 + index);
    char checksum = (char) ('A' + (index % 26));
    char entity = (char) ('1' + (index % 9));
    char additional = (char) ('A' + ((index * 2) % 26));

    return String.format("%02d%s%c%cZ%c", stateCode, pan, checksum, entity, additional);
  }

  private String generatePostalCode() {
    return String.format("%06d", 100001 + random.nextInt(899999));
  }

  private State getStateForCity(String city) {
    return switch (city) {
      case "Delhi" -> State.DELHI;
      case "Bangalore" -> State.KARNATAKA;
      case "Hyderabad" -> State.TELANGANA;
      case "Chennai" -> State.TAMIL_NADU;
      case "Kolkata" -> State.WEST_BENGAL;
      case "Ahmedabad", "Surat" -> State.GUJARAT;
      case "Jaipur" -> State.RAJASTHAN;
      default -> State.MAHARASHTRA;
    };
  }
}
