package com.rkscientificindustries.invoice.backend.invoice.pdf;

import java.math.BigDecimal;

public final class InvoicePdfFormattingSupport {
  private InvoicePdfFormattingSupport() {
  }

  public static String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  public static String format(BigDecimal value) {
    return String.format("%,.2f", value != null ? value : BigDecimal.ZERO);
  }

  public static String formatRate(BigDecimal rate) {
    if (rate == null) {
      return "0";
    }
    return rate.stripTrailingZeros().toPlainString();
  }

  public static String formatState(String state) {
    if (state == null || state.isBlank()) {
      return "";
    }
    return state.replace('_', ' ');
  }

  public static String numberToWordsIndian(long number) {
    if (number == 0) {
      return "Zero";
    }

    String[] units = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };
    String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

    return convertIndian(number, units, tens).trim().replaceAll("\\s+", " ");
  }

  private static String convertIndian(long number, String[] units, String[] tens) {
    int range = number < 20 ? 0
        : number < 100 ? 1
          : number < 1_000 ? 2
            : number < 100_000 ? 3
              : number < 10_000_000 ? 4
                : 5;

    return switch (range) {
      case 0 -> units[(int) number];
      case 1 -> tens[(int) (number / 10)] + " " + convertIndian(number % 10, units, tens);
      case 2 -> units[(int) (number / 100)] + " Hundred " + convertIndian(number % 100, units, tens);
      case 3 -> convertIndian(number / 1_000, units, tens) + " Thousand " + convertIndian(number % 1_000, units, tens);
      case 4 -> convertIndian(number / 100_000, units, tens) + " Lakh " + convertIndian(number % 100_000, units, tens);
      default -> convertIndian(number / 10_000_000, units, tens) + " Crore "
          + convertIndian(number % 10_000_000, units, tens);
    };
  }
}

