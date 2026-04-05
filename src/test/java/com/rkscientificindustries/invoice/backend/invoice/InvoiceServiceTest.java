package com.rkscientificindustries.invoice.backend.invoice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

  @Mock
  private InvoiceRepository invoiceRepository;

  @InjectMocks
  private InvoiceService invoiceService;

  @Test
  @DisplayName("Should return this month invoice value")
  void shouldReturnThisMonthInvoiceValue() {
    var startOfMonth = LocalDate.now().withDayOfMonth(1);
    var startOfNextMonth = startOfMonth.plusMonths(1);
    when(invoiceRepository.sumTotalAmountBetweenDates(startOfMonth, startOfNextMonth))
        .thenReturn(new BigDecimal("2400.00"));

    var value = invoiceService.thisMonthInvoiceValue();

    assertThat(value).isEqualByComparingTo("2400.00");
    verify(invoiceRepository).sumTotalAmountBetweenDates(startOfMonth, startOfNextMonth);
  }

  @Test
  @DisplayName("Should return total invoice value")
  void shouldReturnTotalInvoiceValue() {
    when(invoiceRepository.sumTotalAmount()).thenReturn(new BigDecimal("11200.75"));

    var value = invoiceService.totalInvoiceValue();

    assertThat(value).isEqualByComparingTo("11200.75");
    verify(invoiceRepository).sumTotalAmount();
  }

  @Test
  @DisplayName("Should return average invoice value")
  void shouldReturnAverageInvoiceValue() {
    when(invoiceRepository.averageTotalAmount()).thenReturn(new BigDecimal("1450.25"));

    var value = invoiceService.averageInvoiceValue();

    assertThat(value).isEqualByComparingTo("1450.25");
    verify(invoiceRepository).averageTotalAmount();
  }

  @Test
  @DisplayName("Should return recent invoices")
  void shouldReturnRecentInvoices() {
    List<Invoice> invoices = List.of();
    when(invoiceRepository.findLatestFiveByCreatedDate()).thenReturn(invoices);

    var result = invoiceService.findRecentInvoices();

    assertThat(result).isSameAs(invoices);
    verify(invoiceRepository).findLatestFiveByCreatedDate();
  }
}
