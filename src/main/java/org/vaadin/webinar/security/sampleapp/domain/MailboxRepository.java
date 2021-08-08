package org.vaadin.webinar.security.sampleapp.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MailboxRepository extends JpaRepository<Mailbox, Long>, CustomMailboxRepository {

}
