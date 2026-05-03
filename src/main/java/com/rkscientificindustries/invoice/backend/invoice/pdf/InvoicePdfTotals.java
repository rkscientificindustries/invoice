package com.rkscientificindustries.invoice.backend.invoice.pdf;

import java.math.BigDecimal;
import java.util.Map;

public record InvoicePdfTotals(BigDecimal taxableTotal,
                               BigDecimal taxTotal,
                               BigDecimal grandTotal,
                               BigDecimal roundOff,
                               Map<BigDecimal, BigDecimal> taxableByRate,
                               Map<BigDecimal, BigDecimal> taxByRate) {
}
