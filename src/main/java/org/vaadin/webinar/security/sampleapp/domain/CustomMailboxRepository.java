package org.vaadin.webinar.security.sampleapp.domain;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

public interface CustomMailboxRepository {

    List<Mailbox> findMailboxes(Principal principal, Collection<Mailbox.Permission> permissions);
}
