package org.vaadin.webinar.security.sampleapp.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.security.Principal;
import java.time.Instant;

import static java.util.Objects.requireNonNull;

@Entity
public class Message extends AbstractPersistable<Long> {

    private static final int SUBJECT_MAX_LENGTH = 300;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Mailbox sender;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Mailbox recipient;

    @Basic(optional = false)
    @Column(length = SUBJECT_MAX_LENGTH, nullable = false)
    private String subject;

    @Basic(optional = false)
    @Lob
    private String body;

    @Basic(optional = false)
    @Column(nullable = false)
    private Instant timestamp;

    protected Message() {
    }

    public Message(Mailbox sender, Mailbox recipient, String subject, String body, Instant timestamp) {
        this.sender = requireNonNull(sender);
        this.recipient = requireNonNull(recipient);
        this.subject = StringUtils.truncate(requireNonNull(subject), SUBJECT_MAX_LENGTH);
        this.body = requireNonNull(body);
        this.timestamp = requireNonNull(timestamp);
    }

    public Mailbox getSender() {
        return sender;
    }

    public Mailbox getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public boolean canRead(Principal principal) {
        return getSender().hasPermission(principal, Mailbox.Permission.READ)
                || getRecipient().hasPermission(principal, Mailbox.Permission.READ);
    }
}
