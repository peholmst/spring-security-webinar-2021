package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.vaadin.webinar.security.sampleapp.domain.Folder;
import org.vaadin.webinar.security.sampleapp.domain.Mailbox;
import org.vaadin.webinar.security.sampleapp.domain.MailboxReference;
import org.vaadin.webinar.security.sampleapp.domain.Message;
import org.vaadin.webinar.security.sampleapp.security.Roles;
import org.vaadin.webinar.security.sampleapp.service.CurrentSession;
import org.vaadin.webinar.security.sampleapp.service.MailboxAccessManagementService;
import org.vaadin.webinar.security.sampleapp.service.MessageReadService;
import org.vaadin.webinar.security.sampleapp.service.UserLookupService;

import javax.annotation.security.RolesAllowed;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Route(value = "messages", layout = MainAppLayout.class)
@RolesAllowed(Roles.USER)
public class MessageListView extends SplitLayout {

    private final CurrentSession currentSession;
    private final MessageReadService service;
    private final UserLookupService userLookupService;
    private final MailboxAccessManagementService mailboxAccessManagementService;
    private final Grid<Message> messages;
    private final Button manageAccess;
    private final TreeGrid<TreeNode> mailboxes;
    private final Button compose;
    private final MessageSentTopic messageSentTopic;
    private Registration topicRegistration;

    public MessageListView(CurrentSession currentSession,
                           MessageReadService service,
                           UserLookupService userLookupService,
                           MailboxAccessManagementService mailboxAccessManagementService,
                           MessageSentTopic messageSentTopic) {
        this.currentSession = currentSession;
        this.service = service;
        this.userLookupService = userLookupService;
        this.mailboxAccessManagementService = mailboxAccessManagementService;
        this.messageSentTopic = messageSentTopic;

        mailboxes = new TreeGrid<>();
        mailboxes.setSizeFull();
        mailboxes.setItems(service.getMailboxes().stream().map(MailboxTreeNode::new).collect(Collectors.toList()), TreeNode::getChildren);
        mailboxes.setSelectionMode(SelectionMode.SINGLE);
        mailboxes.addHierarchyColumn(TreeNode::getLabel).setHeader("Mailbox");
        mailboxes.addSelectionListener(this::onMailboxSelected);

        manageAccess = new Button("Manage Access", evt -> onManageAccessClicked());
        manageAccess.setEnabled(false);

        messages = new Grid<>();
        messages.setSizeFull();
        messages.setSelectionMode(SelectionMode.SINGLE);
        messages.addColumn(new TextRenderer<>(Message::getSubject))
                .setHeader("Subject");
        messages.addColumn(new TextRenderer<>(msg -> msg.getSender().getName()))
                .setHeader("Sender");
        messages.addColumn(new TextRenderer<>(msg -> formatTimestamp(msg.getTimestamp())))
                .setHeader("Date & Time");
        messages.addColumn(new TextRenderer<>(msg -> msg.getRecipient().getName()))
                .setHeader("Recipient");
        messages.addSelectionListener(this::onMessageSelected);

        compose = new Button("Compose", evt -> onComposeClicked());
        compose.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        compose.setEnabled(false);

        addToPrimary(manageAccess, mailboxes);
        addToSecondary(compose, messages);
        setSizeFull();
        setSplitterPosition(20);
    }

    private void onMailboxSelected(SelectionEvent<Grid<TreeNode>, TreeNode> event) {
        refreshMessages();
        manageAccess.setEnabled(event.getFirstSelectedItem().filter(t -> t.getMailbox().isOwner(currentSession.currentUser())).isPresent());
        compose.setEnabled(event.getFirstSelectedItem().filter(t -> t.getMailbox().hasPermission(currentSession.currentUser(), Mailbox.Permission.WRITE)).isPresent());
    }

    private void refreshMessages() {
        messages.setItems(mailboxes.getSelectionModel().getFirstSelectedItem()
                .filter(TreeNode::isFolder)
                .map(node -> service.getMessages(new MailboxReference(node.getMailbox()), node.getFolder()))
                .orElse(Collections.emptyList()));
    }

    private String formatTimestamp(Instant instant) {
        return instant.atZone(currentSession.getTimeZone()).toLocalDateTime()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT));
    }

    private void onMessageSelected(SelectionEvent<Grid<Message>, Message> event) {
        event.getFirstSelectedItem().ifPresent(message -> getUI().ifPresent(ui -> ui.navigate(MessageView.class, message.getId())));
    }

    private void onManageAccessClicked() {
        mailboxes.getSelectionModel().getFirstSelectedItem().ifPresent(node ->
                new ManageMailboxAccessDialog(new MailboxReference(node.getMailbox()), userLookupService, mailboxAccessManagementService).open()
        );
    }

    private void onComposeClicked() {
        getUI().ifPresent(ui -> ui.navigate(ComposeMessageView.class, mailboxes.getSelectionModel().getFirstSelectedItem().map(t -> t.getMailbox().getId()).orElse(null)));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        topicRegistration = messageSentTopic.subscribe(event -> mailboxes.getSelectionModel().getFirstSelectedItem().ifPresent(node -> {
            if (node.getMailbox().equals(event.getMessage().getRecipient())) {
                getUI().ifPresent(ui -> ui.access(this::refreshMessages));
            }
        }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        topicRegistration.remove();
    }

    public static abstract class TreeNode {

        public abstract String getLabel();

        public abstract boolean isMailbox();

        public abstract boolean isFolder();

        public abstract Collection<TreeNode> getChildren();

        public Folder getFolder() {
            throw new IllegalStateException("This node does not contain a folder");
        }

        public Mailbox getMailbox() {
            throw new IllegalStateException("This node does not contain a mailbox");
        }
    }

    public static class FolderTreeNode extends TreeNode {

        private final MailboxTreeNode parent;
        private final Folder folder;

        public FolderTreeNode(MailboxTreeNode parent, Folder folder) {
            this.parent = parent;
            this.folder = requireNonNull(folder);
        }

        @Override
        public String getLabel() {
            return folder.name();
        }

        @Override
        public boolean isMailbox() {
            return false;
        }

        @Override
        public boolean isFolder() {
            return true;
        }

        @Override
        public Collection<TreeNode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public Mailbox getMailbox() {
            return parent.getMailbox();
        }

        @Override
        public Folder getFolder() {
            return folder;
        }
    }

    public static class MailboxTreeNode extends TreeNode {

        private final Mailbox mailbox;
        private final List<TreeNode> folders;

        public MailboxTreeNode(Mailbox mailbox) {
            this.mailbox = requireNonNull(mailbox);
            folders = Arrays.stream(Folder.values())
                    .map(folder -> new FolderTreeNode(this, folder))
                    .collect(Collectors.toList());
        }

        @Override
        public String getLabel() {
            return mailbox.getName();
        }

        @Override
        public boolean isMailbox() {
            return true;
        }

        @Override
        public boolean isFolder() {
            return false;
        }

        @Override
        public Collection<TreeNode> getChildren() {
            return folders;
        }

        @Override
        public Mailbox getMailbox() {
            return mailbox;
        }
    }
}
