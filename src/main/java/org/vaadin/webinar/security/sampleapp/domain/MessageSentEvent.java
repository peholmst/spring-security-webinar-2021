package org.vaadin.webinar.security.sampleapp.domain;

import java.util.Objects;

public class MessageSentEvent {

    private final Message message;

    public MessageSentEvent(Message message) {
        this.message = Objects.requireNonNull(message);
    }

    public Message getMessage() {
        return message;
    }
}
