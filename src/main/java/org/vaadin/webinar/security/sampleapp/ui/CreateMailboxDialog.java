package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextField;
import org.vaadin.webinar.security.sampleapp.service.UserLookupService;

import java.security.Principal;
import java.util.function.BiFunction;

public class CreateMailboxDialog extends Dialog {

    public CreateMailboxDialog(UserLookupService userLookup,
                               BiFunction<String, Principal, Boolean> onCreate) {
        var name = new TextField("Name");
        name.setWidthFull();
        var owner = new UserLookupComboBox(userLookup);
        owner.setLabel("Owner");
        owner.setWidthFull();
        setWidth(400, Unit.PIXELS);

        var create = new Button("Create Mailbox", evt -> {
            if (onCreate.apply(name.getValue(), owner.getValue())) {
                close();
            }
        });
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        create.setEnabled(false);
        add(new H2("Create Mailbox"), name, owner, create);

        Runnable updateCreateState = () -> {
            create.setEnabled(!name.isEmpty() && !owner.isEmpty());
        };
        name.addValueChangeListener(evt -> updateCreateState.run());
        owner.addValueChangeListener(evt -> updateCreateState.run());
    }
}
