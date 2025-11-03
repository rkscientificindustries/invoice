package com.rkscientificindustries.invoice.backend;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerRepository;
import com.rkscientificindustries.invoice.backend.customer.CustomerType;
import com.rkscientificindustries.invoice.backend.utils.State;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Profile("demo")
public class MockDataLoader {
  private static final String[] COMPANY_NAMES = {
          "Tech Solutions Pvt Ltd", "Global Industries Ltd", "Innovative Systems Inc",
          "Smart Electronics Corp", "Digital Services Ltd", "Modern Enterprises Pvt Ltd",
          "Future Technologies Inc", "Prime Industries Ltd", "Alpha Systems Pvt Ltd",
          "Beta Solutions Corp", "Gamma Electronics Ltd", "Delta Services Inc"
  };
  private static final String[] INDIVIDUAL_NAMES = {
          "Rajesh Kumar", "Priya Sharma", "Amit Patel", "Sneha Singh", "Vikram Reddy",
          "Anita Gupta", "Suresh Mehta", "Neha Verma", "Kiran Joshi", "Ravi Nair"
  };
  private static final String[] STREETS = {
          "MG Road", "Park Street", "Commercial Street", "Mall Road", "Gandhi Road",
          "Station Road", "Market Street", "Main Road", "Civil Lines", "Residency Road"
  };
  private static final String[] CITIES = {
          "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Pune", "Chennai",
          "Kolkata", "Ahmedabad", "Jaipur", "Surat"
  };
  private final CustomerRepository customerRepository;
  private final InvoiceProperties invoiceProperties;
  private final Random random = new Random();

  public MockDataLoader(CustomerRepository customerRepository, InvoiceProperties invoiceProperties) {
    this.customerRepository = customerRepository;
    this.invoiceProperties = invoiceProperties;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void loadData() {
    int numberOfCustomers = invoiceProperties.getCustomers();

    for (int i = 0; i < numberOfCustomers; i++) {
      Customer customer = generateMockCustomer(i);
      customerRepository.save(customer);
    }

    System.out.println("Successfully loaded " + numberOfCustomers + " mock customers");
  }

  private Customer generateMockCustomer(int index) {
    CustomerType type = random.nextBoolean() ? CustomerType.BUSINESS : CustomerType.INDIVIDUAL;
    String name = type == CustomerType.BUSINESS ?
            COMPANY_NAMES[index % COMPANY_NAMES.length] + " " + (index / COMPANY_NAMES.length + 1) :
            INDIVIDUAL_NAMES[index % INDIVIDUAL_NAMES.length] + " " + (index / INDIVIDUAL_NAMES.length + 1);

    String email = generateEmail(name);
    String phone = generatePhone();
    String gstin = generateGSTIN(index);
    String street = (index + 1) + ", " + STREETS[index % STREETS.length];
    String city = CITIES[index % CITIES.length];
    State state = getStateForCity(city);
    String postalCode = generatePostalCode();

    return Customer.of(name, email, phone, type, gstin, street, city, state, postalCode);
  }

  private String generateEmail(String name) {
    String emailName = name.toLowerCase()
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
