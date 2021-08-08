package org.vaadin.webinar.security.sampleapp.service;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.webinar.security.sampleapp.domain.Mailbox;
import org.vaadin.webinar.security.sampleapp.domain.MailboxReference;
import org.vaadin.webinar.security.sampleapp.domain.MailboxRepository;
import org.vaadin.webinar.security.sampleapp.security.Roles;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
@Secured(Roles.USER)
public class MailboxAccessManagementService {

    private final CurrentSession currentSession;
    private final MailboxRepository mailboxRepository;
    private final UserLookupService userLookupService;

    public MailboxAccessManagementService(CurrentSession currentSession,
                                          MailboxRepository mailboxRepository,
                                          UserLookupService userLookupService) {
        this.currentSession = currentSession;
        this.mailboxRepository = mailboxRepository;
        this.userLookupService = userLookupService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<GrantedPrincipal> getMailboxGrantedPrincipals(MailboxReference mailbox) {
        var mbox = getMailboxAndCheckOwnership(mailbox);
        return mbox.getGrantedPermissions()
                .entrySet()
                .stream()
                .map(e -> toGrantedPrincipal(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAccess(MailboxReference mailbox, Principal user) {
        var mbox = getMailboxAndCheckOwnership(mailbox);
        mbox.revokePermission(user);
        mailboxRepository.saveAndFlush(mbox);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void grantAccess(MailboxReference mailbox, Principal user, Mailbox.Permission permission) {
        var mbox = getMailboxAndCheckOwnership(mailbox);
        mbox.grantPermission(user, permission);
        mailboxRepository.saveAndFlush(mbox);
    }

    private Mailbox getMailboxAndCheckOwnership(MailboxReference mailboxReference) {
        var mbox = mailboxRepository.findById(mailboxReference.getId()).orElseThrow(() -> new IncorrectResultSizeDataAccessException(1));
        if (!mbox.isOwner(currentSession.currentUser())) {
            throw new AccessDeniedException("Only the owner of a mailbox can manage access to it");
        }
        return mbox;
    }

    private GrantedPrincipal toGrantedPrincipal(Principal principal, Mailbox.Permission permission) {
        var fullName = userLookupService.findByPrincipal(principal).blockOptional().map(UserInfo::getFullName).orElse(principal.getName());
        return new GrantedPrincipal(principal, fullName, permission);
    }

    public static class GrantedPrincipal {

        private final Principal principal;
        private final String fullName;
        private final Mailbox.Permission permission;

        GrantedPrincipal(Principal principal, String fullName, Mailbox.Permission permission) {
            this.principal = requireNonNull(principal);
            this.fullName = requireNonNull(fullName);
            this.permission = requireNonNull(permission);
        }

        public Principal getPrincipal() {
            return principal;
        }

        public String getFullName() {
            return fullName;
        }

        public Mailbox.Permission getPermission() {
            return permission;
        }
    }
}
