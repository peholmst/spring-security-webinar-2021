package org.vaadin.webinar.security.sampleapp.service;

import java.security.Principal;

/**
 * TODO Document me
 */
public interface UserInfo extends Principal {

    String getUsername();

    String getFirstName();

    String getLastName();

    default String getFullName() {
        var firstName = getFirstName();
        var lastName = getLastName();

        var sb = new StringBuilder();
        if (!firstName.isBlank()) {
            sb.append(firstName);
        }
        if (!lastName.isBlank()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(lastName);
        }
        if (sb.length() == 0) {
            sb.append(getUsername());
        }
        return sb.toString();
    }
}
