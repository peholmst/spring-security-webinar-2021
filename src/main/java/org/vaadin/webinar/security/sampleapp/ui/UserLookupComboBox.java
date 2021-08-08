package org.vaadin.webinar.security.sampleapp.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import org.vaadin.webinar.security.sampleapp.service.UserInfo;
import org.vaadin.webinar.security.sampleapp.service.UserLookupService;

import java.security.Principal;
import java.util.stream.Collectors;

public class UserLookupComboBox extends ComboBox<Principal> {

    public UserLookupComboBox(UserLookupService userLookupService) {
        super();
        // Please note: don't use this as example code for production applications. It is something I just
        // threw together for this sample and will either perform bad or act strangely in certain cases.
        setItemLabelGenerator(principal -> {
            if (principal instanceof UserInfo) {
                return ((UserInfo) principal).getFullName();
            } else {
                return userLookupService
                        .findByPrincipal(principal)
                        .blockOptional()
                        .map(UserInfo::getFullName)
                        .orElse(principal.getName());
            }
        });
        addFilterChangeListener(evt -> {
            if (evt.isFromClient()) {
                userLookupService.findUsers(evt.getFilter())
                        .subscribe(users ->
                                getUI().ifPresent(ui ->
                                        ui.access(() -> {
                                            var oldValue = getValue();
                                            var items = users
                                                    .stream()
                                                    .map(Principal.class::cast)
                                                    .collect(Collectors.toList());
                                            setItems(items);
                                            setValue(oldValue);
                                        })));
            }
        });
    }
}
