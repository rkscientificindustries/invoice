package com.rkscientificindustries.invoice.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;

@Layout
public class MainLayout extends AppLayout implements AfterNavigationObserver {
  private Span appName;

  public MainLayout() {
    var toggle = new DrawerToggle();
    addToDrawer(getSideNav());
    addToNavbar(toggle, createHeader());
    setPrimarySection(Section.DRAWER);
  }

  private Div createHeader() {
    appName = new Span("Invoice Service");
    appName.getStyle().set("font-size", "var(--aura-font-size-xl)");
    return new Div(appName);
  }

  private SideNav getSideNav() {
    var nav = new SideNav();
    nav.addItem(
        new SideNavItem("Home", "", VaadinIcon.HOME.create()),
        new SideNavItem("Invoices", "/invoices", VaadinIcon.INVOICE.create()),
        new SideNavItem("Customers", "/customers", VaadinIcon.USER_CHECK.create()),
        new SideNavItem("Products", "/products", VaadinIcon.PACKAGE.create())
    );
    return nav;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    String title = "Invoice Service";
    if (getContent() != null) {
      PageTitle pageTitle = getContent().getClass().getAnnotation(PageTitle.class);
      if (pageTitle != null) {
        title = pageTitle.value();
      }
    }
    appName.setText(title);
  }
}
