package com.rkscientificindustries.invoice.backend.schema;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.rkscientificindustries.invoice.TestcontainersConfiguration;
import com.rkscientificindustries.invoice.backend.config.DataConfig;

@DataJdbcTest
@Import({TestcontainersConfiguration.class, DataConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchemaConstraintRegressionTest {

    private static final String CUSTOMER_INSERT_SQL = """
      INSERT INTO customers (
          name,
          email,
          phone,
          street,
          city,
          state,
          postal_code,
          country,
          type,
          gstin
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

    private static final String PRODUCT_INSERT_SQL = """
      INSERT INTO products (
          name,
          description,
          hsn_code,
          unit,
          unit_price,
          cost_price,
          type,
          gst_rate,
          vendor_name
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCustomerCases")
    void shouldRejectInvalidCustomerRows(String description, String expectedConstraint, Object[] args) {
        assertRejected(CUSTOMER_INSERT_SQL, expectedConstraint, args);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidProductCases")
    void shouldRejectInvalidProductRows(String description, String expectedConstraint, Object[] args) {
        assertRejected(PRODUCT_INSERT_SQL, expectedConstraint, args);
    }

    private void assertRejected(String sql, String expectedConstraint, Object[] args) {
        DataIntegrityViolationException exception = catchThrowableOfType(
                () -> jdbcTemplate.update(sql, args),
                DataIntegrityViolationException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getMostSpecificCause())
                .hasMessageContaining(expectedConstraint);
    }

    private static Stream<Arguments> invalidCustomerCases() {
        return Stream.of(
                Arguments.of(
                        "reject customer postal code that does not match the six digit pattern",
                        "chk_customers_postal_code",
                        new Object[]{
                            "Acme Corp",
                            null,
                            null,
                            "123 Business Rd",
                            "Pune",
                            "MAHARASHTRA",
                            "000000",
                            "India",
                            "BUSINESS",
                            "22AAAAA0000A1Z5"
                        }
                ),
                Arguments.of(
                        "reject customer state that is not part of the enum-backed allow list",
                        "chk_customers_state",
                        new Object[]{
                            "Acme Corp",
                            null,
                            null,
                            "123 Business Rd",
                            "Pune",
                            "NOT_A_STATE",
                            "400001",
                            "India",
                            "BUSINESS",
                            "22AAAAA0001A1Z5"
                        }
                ),
                Arguments.of(
                        "reject customer type that is not part of the enum-backed allow list",
                        "chk_customers_type",
                        new Object[]{
                            "Acme Corp",
                            null,
                            null,
                            "123 Business Rd",
                            "Pune",
                            "MAHARASHTRA",
                            "400001",
                            "India",
                            "RESELLER",
                            "22AAAAA0002A1Z5"
                        }
                ),
                Arguments.of(
                        "reject customer GSTIN that does not match the database pattern",
                        "chk_customers_gstin",
                        new Object[]{
                            "Acme Corp",
                            null,
                            null,
                            "123 Business Rd",
                            "Pune",
                            "MAHARASHTRA",
                            "400001",
                            "India",
                            "BUSINESS",
                            "22aaaaa0000A1Z5"
                        }
                )
        );
    }

    private static Stream<Arguments> invalidProductCases() {
        return Stream.of(
                Arguments.of(
                        "reject product HSN code that is not four or six digits",
                        "chk_products_hsn_code",
                        new Object[]{
                            "Beaker 1 Ltr",
                            "NOT_AVAILABLE",
                            "12A4",
                            "PCS",
                            new BigDecimal("295.00"),
                            new BigDecimal("0.00"),
                            "BO",
                            new BigDecimal("18.00"),
                            "NOT_AVAILABLE"
                        }
                ),
                Arguments.of(
                        "reject product unit that is not part of the enum-backed allow list",
                        "chk_products_unit",
                        new Object[]{
                            "Beaker 1 Ltr",
                            "NOT_AVAILABLE",
                            "7017",
                            "BOX",
                            new BigDecimal("295.00"),
                            new BigDecimal("0.00"),
                            "BO",
                            new BigDecimal("18.00"),
                            "NOT_AVAILABLE"
                        }
                ),
                Arguments.of(
                        "reject product type that is not part of the enum-backed allow list",
                        "chk_products_type",
                        new Object[]{
                            "Beaker 1 Ltr",
                            "NOT_AVAILABLE",
                            "7017",
                            "PCS",
                            new BigDecimal("295.00"),
                            new BigDecimal("0.00"),
                            "SELL",
                            new BigDecimal("18.00"),
                            "NOT_AVAILABLE"
                        }
                ),
                Arguments.of(
                        "reject product unit price that is below the minimum allowed value",
                        "chk_products_unit_price",
                        new Object[]{
                            "Beaker 1 Ltr",
                            "NOT_AVAILABLE",
                            "7017",
                            "PCS",
                            new BigDecimal("-1.00"),
                            new BigDecimal("0.00"),
                            "BO",
                            new BigDecimal("18.00"),
                            "NOT_AVAILABLE"
                        }
                )
        );
    }
}
