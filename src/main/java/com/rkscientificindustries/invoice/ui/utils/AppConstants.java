package com.rkscientificindustries.invoice.ui.utils;

import java.math.BigDecimal;

public final class AppConstants {
  private AppConstants() {}

  // Layout Widths
  public static final String DIALOG_WIDTH = "720px";
  public static final String MASTER_MIN_WIDTH = "400px";
  public static final String DETAIL_WIDTH_NARROW = "500px";
  public static final String DETAIL_WIDTH_WIDE = "600px";
  
  // Grid Columns
  public static final String INDEX_COLUMN_WIDTH = "64px";

  // Business Defaults
  public static final BigDecimal DEFAULT_GST_RATE = BigDecimal.valueOf(18);
}
