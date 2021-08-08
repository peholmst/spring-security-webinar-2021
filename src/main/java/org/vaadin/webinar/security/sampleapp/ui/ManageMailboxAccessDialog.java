package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.vaadin.webinar.security.sampleapp.domain.Mailbox;
import org.vaadin.webinar.security.sampleapp.domain.MailboxReference;
import org.vaadin.webinar.security.sampleapp.service.MailboxAccessManagementService;
import org.vaadin.webinar.security.sampleapp.service.UserLookupService;

import static java.util.Objects.requireNonNull;

public class ManageMailboxAccessDialog extends Dialog {

    private final MailboxReference mailbox;
    private final MailboxAccessManagementService service;
    private final Grid<MailboxAccessManagementService.GrantedPrincipal> grantedPrincipals;
    private final UserLookupComboBox user;
    private final ComboBox<Mailbox.Permission> permission;
    private final Button grant;
    private final Button revoke;

    public ManageMailboxAccessDialog(MailboxReference mailbox,
                                     UserLookupService userLookupService,
                                     MailboxAccessManagementService service) {
        this.mailbox = requireNonNull(mailbox);
        this.service = requireNonNull(service);

        user = new UserLookupComboBox(userLookupService);
        user.setLabel("Grant access to");
        user.addValueChangeListener(evt -> updateGrantButtonState());
        permission = new ComboBox<>("Permission", Mailbox.Permission.values());
        permission.addValueChangeListener(evt -> updateGrantButtonState());
        add(new HorizontalLayout(user, permission));
        grant = new Button("Grant Access", evt -> grant());
        grant.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(grant);

        grantedPrincipals = new Grid<>();
        grantedPrincipals.setSelectionMode(Grid.SelectionMode.SINGLE);
        grantedPrincipals
                .addColumn(MailboxAccessManagementService.GrantedPrincipal::getFullName)
                .setHeader("User");
        grantedPrincipals.addColumn(MailboxAccessManagementService.GrantedPrincipal::getPermission)
                .setHeader("Permission");
        grantedPrincipals.addSelectionListener(evt -> updateRevokeButtonState());
        add(grantedPrincipals);

        revoke = new Button("Revoke Access", evt -> revoke());
        revoke.addThemeVariants(ButtonVariant.LUMO_ERROR);
        add(revoke);

        refresh();
    }

    private void revoke() {
        grantedPrincipals
                .getSelectionModel()
                .getFirstSelectedItem()
                .ifPresent(principal -> service.revokeAccess(mailbox, principal.getPrincipal()));
        refresh();
    }

    private void grant() {
        var principal = user.getValue();
        var permission = this.permission.getValue();
        if (principal != null && permission != null) {
            service.grantAccess(mailbox, principal, permission);
            refresh();
            user.clear();
            this.permission.clear();
        }
    }

    private void refresh() {
        grantedPrincipals.setItems(service.getMailboxGrantedPrincipals(mailbox));
    }

    private void updateGrantButtonState() {
        grant.setEnabled(!user.isEmpty() && !permission.isEmpty());
    }

    private void updateRevokeButtonState() {
        revoke.setEnabled(grantedPrincipals.getSelectedItems().size() == 1);
    }
}
