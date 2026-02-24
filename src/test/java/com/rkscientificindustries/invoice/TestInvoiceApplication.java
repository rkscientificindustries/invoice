package com.rkscientificindustries.invoice;

import org.springframework.boot.SpringApplication;

public class TestInvoiceApplication {
  static void main(String[] args) {
    SpringApplication.from(InvoiceApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
