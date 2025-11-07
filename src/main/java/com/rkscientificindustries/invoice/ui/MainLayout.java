package com.rkscientificindustries.invoice.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

import static com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import static com.vaadin.flow.theme.lumo.LumoUtility.Display;
import static com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import static com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import static com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import static com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import static com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import static com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

@Layout
public class MainLayout extends AppLayout {

  public MainLayout() {
    DrawerToggle toggle = new DrawerToggle();

    H1 title = new H1("Dashboard");
    title.getStyle()
            .set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0");

    addToDrawer(getSideNav());
    addToNavbar(toggle, createHeader());

    setPrimarySection(Section.DRAWER);
  }

  private Div createHeader() {

    var appName = new Span("Home");
    appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

    var header = new Div(appName);
    header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
    return header;
  }

  private SideNav getSideNav() {
    var nav = new SideNav();
    nav.addItem(
            new SideNavItem("Home", "", VaadinIcon.HOME.create()),
            new SideNavItem("Customers", "/customers", VaadinIcon.USER_CHECK.create())
    );
    return nav;
  }
}
