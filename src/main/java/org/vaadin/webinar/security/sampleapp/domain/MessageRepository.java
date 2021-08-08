package org.vaadin.webinar.security.sampleapp.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long>, CustomMessageRepository {

}
