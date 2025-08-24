package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
public class StartView extends HorizontalLayout {
    public StartView() {
        this.add(new H1("Please select a stored game, or create a new one."));
    }
}