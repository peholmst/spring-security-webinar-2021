package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import org.owasp.html.PolicyFactory;
import org.vaadin.webinar.security.sampleapp.domain.Message;
import org.vaadin.webinar.security.sampleapp.domain.MessageReference;
import org.vaadin.webinar.security.sampleapp.security.Roles;
import org.vaadin.webinar.security.sampleapp.service.MessageReadService;

import javax.annotation.security.RolesAllowed;

@Route(value = "message", layout = MainAppLayout.class)
@RolesAllowed(Roles.USER)
@CssImport("./styles/message-view.css")
public class MessageView extends VerticalLayout implements HasUrlParameter<Long> {

    private final MessageReadService service;
    private final PolicyFactory htmlSanitizer;

    public MessageView(MessageReadService service, PolicyFactory htmlSanitizer) {
        this.service = service;
        this.htmlSanitizer = htmlSanitizer;
        setSizeFull();
        setSpacing(false);
    }

    private void showMessage(Message message) {
        removeAll();
        var subject = new Div(new Text(message.getSubject()));
        subject.addClassName("message-subject");

        var sender = new Div(new Text("From " + message.getSender().getName()));
        sender.addClassName("message-sender");

        var recipient = new Div(new Text("to " + message.getRecipient().getName()));
        recipient.addClassName("message-recipient");

        var body = new Div(new Html("<span>" + htmlSanitizer.sanitize(message.getBody()) + "</span>"));
        body.setSizeFull();
        body.addClassName("message-body");

        add(subject, sender, recipient, body);
    }

    private void showNotFound() {
        removeAll();
        add(new Span("Message not found"));
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        service.getMessage(new MessageReference(parameter)).ifPresentOrElse(this::showMessage, this::showNotFound);
    }
}
