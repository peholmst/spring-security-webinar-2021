package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.webinar.security.sampleapp.domain.Mailbox;
import org.vaadin.webinar.security.sampleapp.security.Roles;
import org.vaadin.webinar.security.sampleapp.service.MailboxAdminService;
import org.vaadin.webinar.security.sampleapp.service.UserLookupService;

import javax.annotation.security.RolesAllowed;

import static java.util.Objects.requireNonNull;

@Route(value = "admin/mailbox", layout = MainAppLayout.class)
@RolesAllowed(Roles.ADMIN)
public class MailboxAdminView extends VerticalLayout {

    private final MailboxAdminService service;
    private final UserLookupService userLookup;
    private final Grid<Mailbox> mailboxes;

    public MailboxAdminView(MailboxAdminService service, UserLookupService userLookup) {
        this.service = requireNonNull(service);
        this.userLookup = requireNonNull(userLookup);

        mailboxes = new Grid<>();
        mailboxes.setSizeFull();
        mailboxes.addColumn(Mailbox::getName).setHeader("Name");
        mailboxes.setSelectionMode(Grid.SelectionMode.SINGLE);

        Button create = new Button("Create Mailbox", VaadinIcon.PLUS_CIRCLE_O.create(), evt -> create());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button refresh = new Button(VaadinIcon.REFRESH.create(), evt -> refresh());

        add(new HorizontalLayout(create, refresh), mailboxes);
        setSizeFull();

        refresh();
    }

    private void create() {
        new CreateMailboxDialog(userLookup, (name, owner) -> {
            service.createMailbox(name, owner);
            // TODO Handle errors (such as creating a mailbox with the same name as an existing one)
            refresh();
            return true;
        }).open();
    }

    private void refresh() {
        mailboxes.setItems(service.getMailboxes());
    }
}
