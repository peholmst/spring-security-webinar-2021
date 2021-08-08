package org.vaadin.webinar.security.sampleapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.webinar.security.sampleapp.domain.Mailbox;
import org.vaadin.webinar.security.sampleapp.domain.MailboxRepository;
import org.vaadin.webinar.security.sampleapp.security.Roles;

import java.security.Principal;
import java.util.List;

@Service
@Secured(Roles.ADMIN)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailboxAdminService {

    private static final Logger log = LoggerFactory.getLogger(MailboxAdminService.class);

    private final MailboxRepository mailboxRepository;

    public MailboxAdminService(MailboxRepository mailboxRepository) {
        this.mailboxRepository = mailboxRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Mailbox> getMailboxes() {
        return mailboxRepository.findAll();
    }

    public void createMailbox(String name, Principal owner) {
        log.debug("Creating new mailbox with name [{}] for owner [{}]", name, owner);
        mailboxRepository.saveAndFlush(new Mailbox(name, owner));
    }
}
