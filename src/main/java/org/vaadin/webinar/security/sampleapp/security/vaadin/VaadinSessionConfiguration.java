package org.vaadin.webinar.security.sampleapp.security.vaadin;

import com.vaadin.flow.server.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Configures Vaadin to work properly with sessions.
 */
@Component
class VaadinSessionConfiguration implements VaadinServiceInitListener, SystemMessagesProvider, SessionDestroyListener {

    private final String relativeSessionExpiredUrl;

    VaadinSessionConfiguration(ServerProperties serverProperties) {
        relativeSessionExpiredUrl = UriComponentsBuilder.fromPath(serverProperties.getServlet().getContextPath()).path("session-expired").build().toUriString();
    }

    @Override
    public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
        var messages = new CustomizedSystemMessages();
        // Redirect to a specific screen when the session expires. In this particular case we don't want to logout
        // just yet. If you would like the user to be completely logged out when the session expires, this URL
        // should the logout URL.
        messages.setSessionExpiredURL(relativeSessionExpiredUrl);
        return messages;
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
        // We also want to destroy the underlying HTTP session since it is the one that contains the authentication
        // token.
        try {
            event.getSession().getSession().invalidate();
        } catch (Exception ignore) {
            // Session was probably already invalidated.
        }
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().setSystemMessagesProvider(this);
        event.getSource().addSessionDestroyListener(this);
    }
}
