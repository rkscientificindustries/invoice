package com.rkscientificindustries.invoice;

import com.rkscientificindustries.invoice.backend.config.InvoiceProperties;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.aura.Aura;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InvoiceProperties.class)
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css")
public class InvoiceApplication implements AppShellConfigurator {

  public static void main(String[] args) {
    SpringApplication.run(InvoiceApplication.class, args);
  }

}
