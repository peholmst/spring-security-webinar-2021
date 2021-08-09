package org.vaadin.webinar.security.sampleapp.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for feeding the "logged out" and "session expired" static pages.
 */
@Controller
class StaticPagesController {

    @GetMapping("/logged-out")
    public String loggedOut() {
        return "logged-out";
    }

    @GetMapping("/session-expired")
    public String sessionExpired() {
        return "session-expired";
    }
}
