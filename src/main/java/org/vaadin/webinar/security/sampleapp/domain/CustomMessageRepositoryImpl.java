package org.vaadin.webinar.security.sampleapp.domain;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

@Component
class CustomMessageRepositoryImpl implements CustomMessageRepository {

    private final EntityManager entityManager;

    CustomMessageRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Message> findMessages(Mailbox mailbox, Folder folder) {
        if (folder == Folder.INBOX) {
            var query = entityManager.createQuery("SELECT m FROM Message m WHERE m.recipient = :mailbox", Message.class);
            query.setParameter("mailbox", mailbox);
            return query.getResultList();
        } else if (folder == Folder.SENT) {
            var query = entityManager.createQuery("SELECT m FROM Message m WHERE m.sender = :mailbox", Message.class);
            query.setParameter("mailbox", mailbox);
            return query.getResultList();
        } else {
            return Collections.emptyList();
        }
    }
}
