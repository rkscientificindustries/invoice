package com.rkscientificindustries.invoice.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Layout
@PermitAll
public class MainLayout extends AppLayout implements AfterNavigationObserver {
  private Span viewName;

  public MainLayout() {
    var toggle = new DrawerToggle();
    addToDrawer(getSideNav());
    addToNavbar(toggle, createHeader());
    setPrimarySection(Section.DRAWER);
  }

  private HorizontalLayout createHeader() {
    var headerLayout = new HorizontalLayout();
    headerLayout.setWidthFull();
    headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
    headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

    viewName = new Span("");
    viewName.getStyle().set("font-size", "var(--aura-font-size-xl)");

    var logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
    logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    logoutButton.addClickListener(_ -> handleLogout());

    headerLayout.add(viewName, logoutButton);
    return headerLayout;
  }

  private void handleLogout() {
    var logoutHandler = new SecurityContextLogoutHandler();
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    logoutHandler.logout(
        VaadinServletRequest.getCurrent().getHttpServletRequest(),
        VaadinServletResponse.getCurrent().getHttpServletResponse(),
        authentication);
    UI.getCurrent().navigate("login");
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
    viewName.setText(title);
  }
}
