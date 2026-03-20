package com.rkscientificindustries.invoice.ui.utils;

import com.rkscientificindustries.invoice.backend.invoice.InvoiceStatus;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.math.BigDecimal;

public final class InvoiceUtils {
  private InvoiceUtils() {
  }

  public static Badge buildStatusBadge(InvoiceStatus status) {
    var badge = new Badge(status.name());
    switch (status) {
      case DRAFT -> badge.addThemeVariants(BadgeVariant.WARNING);
      case FINALIZED -> badge.addThemeVariants(BadgeVariant.SUCCESS);
    }
    return badge;
  }

  public static Badge buildTaxBadge(BigDecimal rate) {
    var badge = new Badge(rate.stripTrailingZeros().toPlainString() + "% GST");
    switch (rate.intValue()) {
      case 5 -> badge.addThemeVariants(BadgeVariant.CONTRAST);
      case 18 -> badge.addThemeVariants(BadgeVariant.ERROR);
    }
    return badge;
  }

  public static void showNotification(String msg, NotificationVariant type) {
    var n = Notification.show(msg);
    n.addThemeVariants(type);
    n.setPosition(Notification.Position.BOTTOM_CENTER);
  }
}
