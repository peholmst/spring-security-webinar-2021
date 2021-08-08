package org.vaadin.webinar.security.sampleapp.service;

import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.webinar.security.sampleapp.domain.*;
import org.vaadin.webinar.security.sampleapp.security.Roles;

import java.time.Clock;
import java.util.List;
import java.util.Set;

@Service
@Secured(Roles.USER)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MessageCompositionService {

    private static final Logger log = LoggerFactory.getLogger(MessageCompositionService.class);
    private final Clock clock;
    private final MessageRepository messageRepository;
    private final MailboxRepository mailboxRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrentSession currentSession;
    private final PolicyFactory htmlSanitizer;

    public MessageCompositionService(Clock clock,
                                     MessageRepository messageRepository,
                                     MailboxRepository mailboxRepository,
                                     ApplicationEventPublisher applicationEventPublisher,
                                     CurrentSession currentSession,
                                     PolicyFactory policyFactory) {
        this.clock = clock;
        this.messageRepository = messageRepository;
        this.mailboxRepository = mailboxRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.currentSession = currentSession;
        this.htmlSanitizer = policyFactory;
    }

    public void sendMessage(MailboxReference sender, MailboxReference recipient, String subject, String body) {
        var senderMbox = mailboxRepository
                .findById(sender.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sender mailbox does not exist"));
        var recipientMbox = mailboxRepository
                .findById(recipient.getId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient mailbox does not exist"));
        // It is important that the senderMbox has been retrieved from the database so that we can be sure
        // the permissions are correct. Otherwise, somebody outside the service could have tampered with the
        // Mailbox object and then passed it in, allowing write access to a user that does not actually hold it.
        if (!senderMbox.hasPermission(currentSession.currentUser(), Mailbox.Permission.WRITE)) {
            throw new AccessDeniedException("Current user cannot send messages from given mailbox");
        }
        log.debug("Sending message from [{}] to [{}]", sender, recipient);
        var message = new Message(senderMbox, recipientMbox, subject, htmlSanitizer.sanitize(body), clock.instant());
        messageRepository.saveAndFlush(message);
        applicationEventPublisher.publishEvent(new MessageSentEvent(message));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Mailbox> getSenders() {
        return mailboxRepository.findMailboxes(currentSession.currentUser(), Set.of(Mailbox.Permission.WRITE));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Mailbox> getRecipients() {
        return mailboxRepository.findAll();
    }
}
