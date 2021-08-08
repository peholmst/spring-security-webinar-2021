package org.vaadin.webinar.security.sampleapp.domain;

public class MailboxReference extends Reference<Long, Mailbox> {

    public MailboxReference(Long id) {
        super(id, Mailbox.class);
    }

    public MailboxReference(Mailbox entity) {
        super(entity);
    }
}
