package org.vaadin.webinar.security.sampleapp.service;

import java.time.ZoneId;
import java.util.Optional;

/**
 * TODO Document me
 */
public interface CurrentSession {

    Optional<UserInfo> getCurrentUser();

    default UserInfo currentUser() {
        return getCurrentUser().orElseThrow(() -> new IllegalStateException("No user bound to current thread"));
    }

    ZoneId getTimeZone();

    boolean hasRole(String role);
}
