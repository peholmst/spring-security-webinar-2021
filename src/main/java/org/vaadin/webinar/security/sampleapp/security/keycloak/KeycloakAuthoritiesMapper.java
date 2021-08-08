package org.vaadin.webinar.security.sampleapp.security.keycloak;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Our Keycloak instance has been configured to expose the client roles inside the ID token under the claim
 * <code>resource_access.${client_id}.roles</code>. This mapper will fetch the roles from that claim and convert
 * them into <code>ROLE_</code> {@link GrantedAuthority authorities} that can be used directly by Spring Security.
 */
@Component
class KeycloakAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private final String clientId;

    KeycloakAuthoritiesMapper(@Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .filter(OAuth2UserAuthority.class::isInstance)
                .map(OAuth2UserAuthority.class::cast)
                .findFirst()
                .map(this::extractClientRoles)
                .orElse(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractClientRoles(OAuth2UserAuthority oauthAuthority) {
        var resourceAccess = (Map<String, Object>) oauthAuthority.getAttributes().getOrDefault("resource_access", Collections.emptyMap());
        var clientAccess = (Map<String, Object>) resourceAccess.getOrDefault(clientId, Collections.emptyMap());
        var roles = (Collection<String>) clientAccess.get("roles");
        // Keycloak has been configured not to prepend a prefix to the roles. We want to do that here instead.
        return roles.stream().map(r -> "ROLE_" + r.toUpperCase()).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }
}
