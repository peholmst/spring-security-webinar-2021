package org.vaadin.webinar.security.sampleapp.domain;

import java.util.List;

public interface CustomMessageRepository {

    List<Message> findMessages(Mailbox mailbox, Folder folder);
}
