package org.vaadin.webinar.security.sampleapp.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.webinar.security.sampleapp.domain.*;
import org.vaadin.webinar.security.sampleapp.security.Roles;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Secured(Roles.USER)
@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
public class MessageReadService {

    private final CurrentSession currentSession;
    private final MailboxRepository mailboxRepository;
    private final MessageRepository messageRepository;

    public MessageReadService(CurrentSession currentSession,
                              MailboxRepository mailboxRepository,
                              MessageRepository messageRepository) {
        this.currentSession = currentSession;
        this.mailboxRepository = mailboxRepository;
        this.messageRepository = messageRepository;
    }

    public List<Mailbox> getMailboxes() {
        return mailboxRepository.findMailboxes(currentSession.currentUser(),
                Set.of(Mailbox.Permission.READ, Mailbox.Permission.WRITE));
    }

    public List<Message> getMessages(MailboxReference mailbox, Folder folder) {
        return mailboxRepository
                .findById(mailbox.getId())
                .filter(m -> m.hasPermission(currentSession.currentUser(), Mailbox.Permission.READ))
                .map(m -> messageRepository.findMessages(m, folder))
                .orElse(Collections.emptyList());
    }

    public Optional<Message> getMessage(MessageReference messageReference) {
        return messageRepository
                .findById(messageReference.getId())
                .filter(m -> m.canRead(currentSession.currentUser()));
    }
}
