package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.shared.Registration;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.vaadin.webinar.security.sampleapp.domain.MessageSentEvent;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This is a quick and dirty way of publishing events from the backend to the UI. Whenever you do this, you have to
 * know what happens to the {@link org.springframework.security.core.context.SecurityContext}. Remember that each UI
 * session has its own context, and you do not want these to get mixed up!
 *
 * @see org.vaadin.webinar.security.sampleapp.security.vaadin.VaadinAwareSecurityContextHolderStrategy
 */
@Component
public class MessageSentTopic implements ApplicationListener<MessageSentEvent> {

    private final ExecutorService executorService;
    private final List<Consumer<MessageSentEvent>> subscribers = new ArrayList<>();

    public MessageSentTopic() {
        executorService = Executors.newCachedThreadPool();
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdown();
    }

    public synchronized Registration subscribe(Consumer<MessageSentEvent> subscriber) {
        subscribers.add(subscriber);
        return () -> {
            synchronized (MessageSentTopic.this) {
                subscribers.remove(subscriber);
            }
        };
    }

    @Override
    public void onApplicationEvent(MessageSentEvent event) {
        List<Consumer<MessageSentEvent>> subscribers;
        synchronized (this) {
            subscribers = List.copyOf(this.subscribers);
        }
        // Invoke the subscribers inside a separate thread and make sure the security context is cleared after
        // each invocation to avoid leaking it from one subscriber to another.
        executorService.submit(() -> subscribers.forEach(subscriber -> {
            try {
                subscriber.accept(event);
            } catch (Exception ignoreIt) {
                // Silently ignore exceptions
            } finally {
                SecurityContextHolder.clearContext();
            }
        }));
        subscribers.forEach(subscriber -> executorService.submit(() -> subscriber.accept(event)));
    }
}
