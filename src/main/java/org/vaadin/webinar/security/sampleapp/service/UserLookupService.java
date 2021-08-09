package org.vaadin.webinar.security.sampleapp.service;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

/**
 * Interface used by other parts of the application to look up information about users.
 */
public interface UserLookupService {

    /**
     * Returns a list of users that match the given query string.
     */
    Mono<List<UserInfo>> findUsers(@Nullable String query);

    /**
     * Returns user information corresponding to the given principal.
     */
    default Mono<UserInfo> findByPrincipal(Principal principal) {
        return findByPrincipalName(principal.getName());
    }

    /**
     * Returns user information corresponding to the given principal name.
     */
    Mono<UserInfo> findByPrincipalName(String name);
}
