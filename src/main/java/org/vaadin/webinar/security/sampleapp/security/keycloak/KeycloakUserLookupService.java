package org.vaadin.webinar.security.sampleapp.security.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.vaadin.webinar.security.sampleapp.security.Roles;
import org.vaadin.webinar.security.sampleapp.service.UserInfo;
import org.vaadin.webinar.security.sampleapp.service.UserLookupService;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Implementation of {@link UserLookupService} that uses the Keycloak REST API to look up users based on a query string.
 */
@Service
@Secured({Roles.USER, Roles.ADMIN})
class KeycloakUserLookupService implements UserLookupService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserLookupService.class);
    private final WebClient client;
    private final String restApiUri;

    KeycloakUserLookupService(ClientRegistrationRepository clientRegistrationRepository,
                              @Value("${keycloak.rest-api-uri}") String restApiUri) {
        var clientRegistration = clientRegistrationRepository.findByRegistrationId("keycloak-rest");
        var clients = new InMemoryReactiveClientRegistrationRepository(clientRegistration);
        var clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clients);
        var clientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clients, clientService);
        var oauth2 = new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2.setDefaultClientRegistrationId(clientRegistration.getRegistrationId());
        client = WebClient.builder().filter(oauth2).build();
        this.restApiUri = restApiUri;
    }

    @Override
    public Mono<List<UserInfo>> findUsers(@Nullable String query) {
        if (query == null || query.isBlank()) {
            return Mono.just(Collections.emptyList());
        }
        return client
                .get()
                .uri(uri().path("/users").queryParam("search", query).build().toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(UserRepresentation[].class)
                .switchIfEmpty(Mono.just(new UserRepresentation[0]))
                .doOnNext(s -> log.debug("Found users: {}", Arrays.toString(s)))
                .map(List::of);
    }

    @Override
    public Mono<UserInfo> findByPrincipalName(String name) {
        return client
                .get()
                .uri(uri().path("/users/{id}").uriVariables(Map.of("id", name)).build().toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(UserRepresentation.class)
                .doOnNext(s -> log.debug("Found user: {}", s))
                .cast(UserInfo.class);
    }

    private UriComponentsBuilder uri() {
        return UriComponentsBuilder.fromUriString(restApiUri);
    }

    @SuppressWarnings("unused")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class UserRepresentation implements UserInfo {

        @JsonProperty
        private String id;
        @JsonProperty
        private String firstName;
        @JsonProperty
        private String lastName;
        @JsonProperty
        private String username;

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }

        @Override
        public String getName() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserRepresentation that = (UserRepresentation) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "UserRepresentation{" +
                    "id='" + id + '\'' +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}
