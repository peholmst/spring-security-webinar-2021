package org.vaadin.webinar.security.sampleapp.domain;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.util.Objects;

public class MessageSentEvent extends ApplicationEvent {

    private final Message message;

    public MessageSentEvent(Object source, Clock clock, Message message) {
        super(source, clock);
        this.message = Objects.requireNonNull(message);
    }

    public Message getMessage() {
        return message;
    }
}
