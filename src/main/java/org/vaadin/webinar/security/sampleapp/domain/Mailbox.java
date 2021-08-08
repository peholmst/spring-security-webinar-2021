package org.vaadin.webinar.security.sampleapp.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Entity
public class Mailbox extends AbstractPersistable<Long> {

    public static final int NAME_MAX_LENGTH = 200;

    @Column(nullable = false)
    private String owner;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @MapKeyColumn(length = NAME_MAX_LENGTH)
    private Map<String, Permission> permissions = new HashMap<>();

    @Column(nullable = false, length = NAME_MAX_LENGTH, unique = true)
    private String name;

    protected Mailbox() {
    }

    public Mailbox(String name, Principal owner) {
        this.name = requireNonNull(StringUtils.truncate(name, NAME_MAX_LENGTH));
        this.owner = requireNonNull(owner.getName());
    }

    public Principal getOwner() {
        return () -> owner;
    }

    public String getName() {
        return name;
    }

    public void transferOwnership(Principal newOwner) {
        this.owner = requireNonNull(newOwner.getName());
    }

    public void grantPermission(Principal grantee, Permission permission) {
        permissions.put(requireNonNull(StringUtils.truncate(grantee.getName(), NAME_MAX_LENGTH)), requireNonNull(permission));
    }

    public void revokePermission(Principal grantee) {
        permissions.remove(grantee.getName());
    }

    public Map<Principal, Permission> getGrantedPermissions() {
        return Collections
                .unmodifiableMap(permissions.entrySet().stream()
                        .collect(Collectors.toMap(e -> e::getKey, Map.Entry::getValue)));
    }

    public boolean hasPermission(Principal user, Permission permission) {
        if (isOwner(user)) {
            return true;
        }
        var actual = permissions.get(user.getName());
        return actual != null && actual.implies(permission);
    }

    public boolean isOwner(Principal user) {
        return user.getName().equals(owner);
    }

    public enum Permission {
        READ, WRITE;

        public boolean implies(Permission permission) {
            return getImpliedPermissions().contains(permission);
        }

        public Set<Permission> getImpliedPermissions() {
            if (this == WRITE) {
                return Set.of(READ, WRITE);
            } else {
                return Set.of(this);
            }
        }
    }
}
