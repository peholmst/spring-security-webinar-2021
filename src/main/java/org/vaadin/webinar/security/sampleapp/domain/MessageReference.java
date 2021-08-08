package org.vaadin.webinar.security.sampleapp.domain;

public class MessageReference extends Reference<Long, Message> {

    public MessageReference(Long id) {
        super(id, Message.class);
    }

    public MessageReference(Message entity) {
        super(entity);
    }
}
