package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import org.vaadin.webinar.security.sampleapp.security.Roles;
import org.vaadin.webinar.security.sampleapp.security.vaadin.LogoutUtil;
import org.vaadin.webinar.security.sampleapp.service.CurrentSession;
import org.vaadin.webinar.security.sampleapp.service.UserInfo;

import java.util.Optional;

@CssImport("./styles/main-app-layout.css")
public class MainAppLayout extends AppLayout {

    private final Tabs tabs;

    public MainAppLayout(CurrentSession currentSession, LogoutUtil logoutUtil) {
        var currentUser = new Div(new Text(currentSession.getCurrentUser().map(UserInfo::getFullName).orElse("")));
        currentUser.addClassName("current-user");
        tabs = new Tabs();
        tabs.setAutoselect(false);
        tabs.add(createTabForView("Start", StartView.class));
        if (currentSession.hasRole(Roles.USER)) {
            tabs.add(createTabForView("Messages", MessageListView.class));
        }
        if (currentSession.hasRole(Roles.ADMIN)) {
            tabs.add(createTabForView("Manage Mailboxes", MailboxAdminView.class));
        }

        var logout = new Button("Logout", evt -> logoutUtil.logout());
        logout.addThemeVariants(ButtonVariant.LUMO_SMALL);
        var layout = new HorizontalLayout(currentUser, tabs, logout);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        addToNavbar(layout);
    }

    private Tab createTabForView(String label, Class<? extends Component> viewClass) {
        var tab = new Tab(new RouterLink(label, viewClass));
        ComponentUtil.setData(tab, Class.class, viewClass);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        tabs.setSelectedTab(getTabForComponent(getContent()).orElse(null));
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return tabs.getChildren()
                .filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }
}
