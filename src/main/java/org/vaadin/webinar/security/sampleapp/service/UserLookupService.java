package org.vaadin.webinar.security.sampleapp.service;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

/**
 * TODO Document me
 */
public interface UserLookupService {

    Mono<List<UserInfo>> findUsers(@Nullable String query);

    default Mono<UserInfo> findByPrincipal(Principal principal) {
        return findByPrincipalName(principal.getName());
    }

    Mono<UserInfo> findByPrincipalName(String name);
}
