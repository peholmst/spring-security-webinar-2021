package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import org.vaadin.webinar.security.sampleapp.domain.Mailbox;
import org.vaadin.webinar.security.sampleapp.domain.MailboxReference;
import org.vaadin.webinar.security.sampleapp.security.Roles;
import org.vaadin.webinar.security.sampleapp.service.MessageCompositionService;

import javax.annotation.security.RolesAllowed;
import java.util.Objects;

@Route(value = "compose", layout = MainAppLayout.class)
@RolesAllowed(Roles.USER)
public class ComposeMessageView extends VerticalLayout implements HasUrlParameter<Long> {

    private final MessageCompositionService service;
    private final ComboBox<Mailbox> sender;
    private final ComboBox<Mailbox> recipient;
    private final TextField subject;
    private final RichTextEditor body;
    private final Button send;

    public ComposeMessageView(MessageCompositionService service) {
        this.service = service;

        sender = new ComboBox<>("From");
        sender.setItems(service.getSenders());
        sender.setItemLabelGenerator(Mailbox::getName);
        sender.setWidthFull();
        sender.addValueChangeListener(evt -> updateSendButtonState());

        recipient = new ComboBox<>("To");
        recipient.setItems(service.getRecipients());
        recipient.setItemLabelGenerator(Mailbox::getName);
        recipient.setWidthFull();
        recipient.addValueChangeListener(evt -> updateSendButtonState());

        subject = new TextField("Subject");
        subject.setWidthFull();
        subject.addValueChangeListener(evt -> updateSendButtonState());

        body = new RichTextEditor();
        body.setSizeFull();
        body.addValueChangeListener(evt -> updateSendButtonState());

        send = new Button("Send", evt -> send());
        send.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(sender, recipient, subject, body, send);
        setSizeFull();

        updateSendButtonState();
    }

    private void send() {
        service.sendMessage(new MailboxReference(sender.getValue()), new MailboxReference(recipient.getValue()),
                subject.getValue(), body.getHtmlValue());
        getUI().ifPresent(ui -> ui.navigate(MessageListView.class));
    }

    private void updateSendButtonState() {
        send.setEnabled(!sender.isEmpty() && !recipient.isEmpty() && !subject.isEmpty() && !body.isEmpty());
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {
        sender.getListDataView().getItems().filter(m -> Objects.equals(m.getId(), parameter)).findFirst().ifPresent(sender::setValue);
    }
}
