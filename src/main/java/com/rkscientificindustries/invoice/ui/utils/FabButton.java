package com.rkscientificindustries.invoice.ui.utils;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Utility class for creating Material Design style Floating Action Buttons (FAB).
 * FABs are circular buttons that float above the UI and represent the primary action.
 */
public final class FabButton {

  private FabButton() {
  }

  /**
   * Creates a primary floating action button with a plus icon.
   *
   * @param tooltipText the tooltip text to display on hover
   * @return configured FAB button without a click listener
   */
  public static Button create(String tooltipText) {
    return create(VaadinIcon.PLUS, tooltipText);
  }

  /**
   * Creates a primary floating action button with a custom icon.
   *
   * @param icon        the Vaadin icon to display
   * @param tooltipText the tooltip text to display on hover
   * @return configured FAB button without a click listener
   */
  public static Button create(VaadinIcon icon, String tooltipText) {
    var button = new Button(new Icon(icon));
    button.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.LARGE);
    button.setTooltipText(tooltipText);
    applyFabStyles(button);
    return button;
  }

  /**
   * Creates a primary floating action button with a plus icon and click listener.
   *
   * @param tooltipText   the tooltip text to display on hover
   * @param clickListener the click event listener
   * @return configured FAB button with click listener attached
   */
  public static Button create(String tooltipText, ComponentEventListener<ClickEvent<Button>> clickListener) {
    return create(VaadinIcon.PLUS, tooltipText, clickListener);
  }

  /**
   * Creates a primary floating action button with a custom icon and click listener.
   *
   * @param icon          the Vaadin icon to display
   * @param tooltipText   the tooltip text to display on hover
   * @param clickListener the click event listener
   * @return configured FAB button with click listener attached
   */
  public static Button create(VaadinIcon icon, String tooltipText,
                              ComponentEventListener<ClickEvent<Button>> clickListener) {
    var button = create(icon, tooltipText);
    button.addClickListener(clickListener);
    return button;
  }

  /**
   * Applies Material Design FAB styling to a button.
   * Styles include fixed positioning at bottom-right, circular shape, and elevation shadow.
   *
   * @param button the button to style
   */
  private static void applyFabStyles(Button button) {
    button.getStyle()
        .set("position", "fixed")
        .set("bottom", "1.5rem")
        .set("right", "5.5rem")
        .set("z-index", "1000")
        .set("border-radius", "50%")
        .set("width", "56px")
        .set("height", "56px")
        .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.2)");
  }
}
