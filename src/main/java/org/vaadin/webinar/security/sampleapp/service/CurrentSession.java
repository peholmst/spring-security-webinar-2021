package org.vaadin.webinar.security.sampleapp.service;

import java.time.ZoneId;
import java.util.Optional;

/**
 * Interface used by other parts of the system to get information about the current session and user, regardless
 * of how that information is stored and retrieved.
 */
public interface CurrentSession {

    /**
     * Gets the current user, if any.
     */
    Optional<UserInfo> getCurrentUser();

    /**
     * Gets the current user and throws an exception if there is none.
     */
    default UserInfo currentUser() {
        return getCurrentUser().orElseThrow(() -> new IllegalStateException("No user bound to current thread"));
    }

    /**
     * Gets the current user's time zone or a default time zone if there is no current user.
     */
    ZoneId getTimeZone();

    /**
     * Checks if the current user has the given role. If there is no current user this method always returns false.
     */
    boolean hasRole(String role);
}
