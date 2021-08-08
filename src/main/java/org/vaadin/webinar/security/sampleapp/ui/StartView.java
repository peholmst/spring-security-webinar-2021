package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.webinar.security.sampleapp.security.Roles;

import javax.annotation.security.RolesAllowed;

@Route(value = "", layout = MainAppLayout.class)
@RolesAllowed({Roles.USER, Roles.ADMIN})
public class StartView extends VerticalLayout {

    public StartView() {
        add(new H1("This is the start view"));
    }
}
