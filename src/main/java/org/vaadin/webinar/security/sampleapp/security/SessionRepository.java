package org.vaadin.webinar.security.sampleapp.security;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A quick and dirty repository of sessions that is used to demonstrate the principle of back channel logout. Don't
 * do this in production code.
 */
public class SessionRepository {

    private final Set<HttpSession> sessions = new HashSet<>();

    public synchronized void add(HttpSession session) {
        sessions.add(session);
    }

    public synchronized void remove(HttpSession session) {
        sessions.remove(session);
    }

    public synchronized void invalidate(Predicate<HttpSession> predicate) {
        Set.copyOf(sessions).stream().filter(predicate).forEach(HttpSession::invalidate);
    }
}
