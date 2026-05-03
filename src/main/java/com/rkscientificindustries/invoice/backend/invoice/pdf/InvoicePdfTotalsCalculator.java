package com.rkscientificindustries.invoice.backend.invoice.pdf;

import com.rkscientificindustries.invoice.backend.invoice.Invoice;
import com.rkscientificindustries.invoice.backend.invoice.LineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;

@Component
public class InvoicePdfTotalsCalculator {
  public InvoicePdfTotals calculate(Invoice invoice) {
    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    var taxableByRate = new TreeMap<BigDecimal, BigDecimal>();
    var taxByRate = new TreeMap<BigDecimal, BigDecimal>();

    for (LineItem item : invoice.getItems()) {
      BigDecimal amount = calculateAmount(item);
      BigDecimal gstRate = item.getGstRate() != null ? item.getGstRate() : BigDecimal.ZERO;
      BigDecimal taxAmount = amount
          .multiply(gstRate)
          .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

      subtotal = subtotal.add(amount);
      totalTax = totalTax.add(taxAmount);
      taxableByRate.merge(gstRate, amount, BigDecimal::add);
      taxByRate.merge(gstRate, taxAmount, BigDecimal::add);
    }

    BigDecimal computedGross = subtotal.add(totalTax).setScale(2, RoundingMode.HALF_UP);
    BigDecimal grandTotal = invoice.getTotalAmount() != null
        ? invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP)
        : computedGross;
    BigDecimal roundOff = grandTotal.subtract(computedGross).setScale(2, RoundingMode.HALF_UP);

    return new InvoicePdfTotals(subtotal, totalTax, grandTotal, roundOff, taxableByRate, taxByRate);
  }

  public BigDecimal calculateAmount(LineItem item) {
    BigDecimal qty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0);
    BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
    return qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
  }
}
