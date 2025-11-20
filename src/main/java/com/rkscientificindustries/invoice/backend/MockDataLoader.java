package com.rkscientificindustries.invoice.backend;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.rkscientificindustries.invoice.backend.customer.Customer;
import com.rkscientificindustries.invoice.backend.customer.CustomerRepository;
import com.rkscientificindustries.invoice.backend.customer.CustomerType;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceItem;
import com.rkscientificindustries.invoice.backend.invoice.InvoiceItemRepository;
import com.rkscientificindustries.invoice.backend.utils.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
@Profile("demo")
public class MockDataLoader {
  private static final Logger logger = LoggerFactory.getLogger(MockDataLoader.class);

  private static final String[] ITEM_NAMES = {
          "Scientific Glassware", "Laboratory Beaker", "Test Tube Set", "Petri Dish",
          "Microscope Slide", "Pipette", "Burette", "Conical Flask", "Measuring Cylinder",
          "Reagent Bottle", "Watch Glass", "Funnel", "Dropper", "Stirring Rod",
          "Crucible", "Evaporating Dish", "Volumetric Flask", "Separating Funnel"
  };
  private static final String[] ITEM_DESCRIPTIONS = {
          "High-quality borosilicate glass", "Precision measuring equipment",
          "Autoclavable laboratory supplies", "Chemical resistant material",
          "Premium laboratory grade", "Durable and reliable", "ISO certified product",
          "Professional laboratory equipment", "Made with precision engineering"
  };
  private static final String[] HSN_CODES = {
          "7017", "701710", "701790", "9027", "902780", "3926", "392690",
          "7020", "702000", "3917", "391790"
  };
  private static final String[] VENDOR_NAMES = {
          "Scientific Suppliers Ltd", "Lab Equipment Co", "Global Lab Mart",
          "Premium Instruments", "Quality Lab Supplies", "TechLab Industries",
          "Elite Scientific", "Pro Lab Solutions", "Apex Laboratory Goods",
          "Superior Science Supplies"
  };
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
  private final InvoiceItemRepository invoiceItemRepository;
  private final InvoiceProperties invoiceProperties;
  private final Random random = new Random();

  public MockDataLoader(CustomerRepository customerRepository,
                        InvoiceItemRepository invoiceItemRepository,
                        InvoiceProperties invoiceProperties) {
    this.customerRepository = customerRepository;
    this.invoiceItemRepository = invoiceItemRepository;
    this.invoiceProperties = invoiceProperties;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void loadData() {
    int numberOfCustomers = invoiceProperties.getCustomers();

    for (int i = 0; i < numberOfCustomers; i++) {
      Customer customer = generateMockCustomer(i);
      customerRepository.save(customer);
    }

    logger.info("Successfully loaded {} mock customers", numberOfCustomers);

    int numberOfItems = invoiceProperties.getItems();

    for (int i = 0; i < numberOfItems; i++) {
      InvoiceItem item = generateMockInvoiceItem(i);
      invoiceItemRepository.save(item);
    }

    logger.info("Successfully loaded {} mock invoice items", numberOfItems);
  }

  private Customer generateMockCustomer(int index) {
    var type = random.nextBoolean() ? CustomerType.BUSINESS : CustomerType.INDIVIDUAL;
    var name = type == CustomerType.BUSINESS ?
            COMPANY_NAMES[index % COMPANY_NAMES.length] + " " + (index / COMPANY_NAMES.length + 1) :
            INDIVIDUAL_NAMES[index % INDIVIDUAL_NAMES.length] + " " + (index / INDIVIDUAL_NAMES.length + 1);

    var email = generateEmail(name);
    var phone = generatePhone();
    var gstin = generateGSTIN(index);
    var street = (index + 1) + ", " + STREETS[index % STREETS.length];
    var city = CITIES[index % CITIES.length];
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

  private InvoiceItem generateMockInvoiceItem(int index) {
    var name = ITEM_NAMES[index % ITEM_NAMES.length];
    if (index >= ITEM_NAMES.length) name += " (" + (index / ITEM_NAMES.length + 1) + ")";

    var description = ITEM_DESCRIPTIONS[index % ITEM_DESCRIPTIONS.length];
    var hsnCode = HSN_CODES[index % HSN_CODES.length];
    var unit = InvoiceItem.Unit.values()[index % InvoiceItem.Unit.values().length];
    var type = InvoiceItem.ItemType.values()[index % InvoiceItem.ItemType.values().length];
    // Only 5% or 18% GST rates allowed
    var gstRate = (index % 2 == 0) ? BigDecimal.valueOf(5) : BigDecimal.valueOf(18);
    // Generate unit price between ₹50 and ₹5000 with 2 decimal places
    double randomPrice = 50 + (random.nextDouble() * 4950);
    var unitPrice = BigDecimal.valueOf(randomPrice).setScale(2, RoundingMode.HALF_UP);
    // Generate cost price (70-90% of unit price)
    var costMultiplier = 0.70 + (random.nextDouble() * 0.20);
    var costPrice = unitPrice.multiply(BigDecimal.valueOf(costMultiplier))
            .setScale(2, java.math.RoundingMode.HALF_UP);
    var vendorName = VENDOR_NAMES[index % VENDOR_NAMES.length];

    return InvoiceItem.of(name, description, hsnCode, unit, unitPrice, costPrice, type, gstRate, vendorName);
  }
}
