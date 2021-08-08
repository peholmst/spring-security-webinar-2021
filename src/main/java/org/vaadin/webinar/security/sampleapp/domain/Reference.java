package org.vaadin.webinar.security.sampleapp.domain;

import org.springframework.data.domain.Persistable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Reference<ID, T extends Persistable<ID>> {

    private final ID id;
    private final Class<T> entityClass;

    public Reference(ID id, Class<T> entityClass) {
        this.id = requireNonNull(id);
        this.entityClass = requireNonNull(entityClass);
    }

    @SuppressWarnings("unchecked")
    public Reference(T entity) {
        this(entity.getId(), (Class<T>) entity.getClass());
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public ID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reference<?, ?> reference = (Reference<?, ?>) o;
        return id.equals(reference.id) && entityClass.equals(reference.entityClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entityClass);
    }
}
