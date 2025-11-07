package com.rkscientificindustries.invoice.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Invoice Manager")
@Route(value = "", layout = MainLayout.class)
public class HomeView extends VerticalLayout {
    public HomeView() {
        add(new H2("Welcome to Invoice Service"));
    }
}

