package org.vaadin.webinar.security.sampleapp.security.vaadin;

import com.vaadin.flow.component.UI;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility used by Vaadin views to log out the current user.
 */
@Component
public class LogoutUtil {

    private final String relativeLogoutUrl;

    LogoutUtil(ServerProperties serverProperties) {
        relativeLogoutUrl = UriComponentsBuilder.fromPath(serverProperties.getServlet().getContextPath()).path("logout").build().toUriString();
    }

    /**
     * Logs out the current user by redirecting the browser to the logout URL.
     */
    public void logout() {
        UI.getCurrent().getPage().setLocation(relativeLogoutUrl);
    }
}
