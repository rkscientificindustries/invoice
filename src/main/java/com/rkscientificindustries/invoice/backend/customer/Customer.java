package com.rkscientificindustries.invoice.backend.customer;

import com.rkscientificindustries.invoice.backend.utils.State;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.Instant;

@Table("customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
  @Id
  private Long id;

  @NotBlank(message = "Customer name cannot be empty")
  @Size(max = 255, message = "Customer name must not exceed 255 characters")
  private String name;

  @Email(message = "Email is not valid")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  String email;

  @Size(max = 20, message = "Phone must not exceed 20 characters")
  private String phone;

  @NotNull
  private CustomerType type;

  @NotBlank(message = "GSTIN cannot be empty")
  @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$", message = "GSTIN must be a valid 15-digit number")
  private String gstin;

  @NotBlank(message = "Street cannot be empty")
  @Size(max = 255, message = "Street must not exceed 255 characters")
  private String street;

  @NotBlank(message = "City cannot be empty")
  @Size(max = 255, message = "City must not exceed 255 characters")
  private String city;

  @NotNull
  private State state;

  @NotBlank(message = "Postal code cannot be empty")
  @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Postal code must be a valid 6-digit number")
  private String postalCode;

  @CreatedDate
  Instant createdDate;

  @LastModifiedDate
  Instant lastModifiedDate;

  @Version
  int version;

  public static Customer of(String name, String email, String phone, CustomerType type, String gstin, String street, String city, State state, String postalCode) {
    return new Customer(null, name, email, phone, type, gstin, street, city, state, postalCode, null, null, 0);
  }
}
