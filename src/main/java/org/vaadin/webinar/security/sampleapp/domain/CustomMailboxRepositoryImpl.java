package org.vaadin.webinar.security.sampleapp.domain;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Component
class CustomMailboxRepositoryImpl implements CustomMailboxRepository {

    private final EntityManager entityManager;

    CustomMailboxRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Mailbox> findMailboxes(Principal principal, Collection<Mailbox.Permission> permissions) {
        var query = entityManager.createQuery("SELECT DISTINCT m FROM Mailbox m LEFT JOIN m.permissions p WHERE (m.owner = :principal) OR (KEY(p) = :principal AND p IN :permissions)", Mailbox.class);
        query.setParameter("principal", principal.getName());
        query.setParameter("permissions", permissions);
        return query.getResultList();
    }
}
