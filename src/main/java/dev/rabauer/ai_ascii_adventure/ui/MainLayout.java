package dev.rabauer.ai_ascii_adventure.ui;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import dev.rabauer.ai_ascii_adventure.persistence.GamePersistenceService;

@Layout
public class MainLayout extends AppLayout {

    private final GamePersistenceService gamePersistenceService;

    public MainLayout(GamePersistenceService gamePersistenceService) {
        this.gamePersistenceService = gamePersistenceService;
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("MyApp");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        Nav nav = getSideNav();

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
        addToNavbar(toggle, title);

        ComponentUtil.addListener(UI.getCurrent(), GameChangeEvent.class, event -> {
            scroller.setContent(getSideNav());
        });
    }

    private Nav getSideNav() {
        VerticalLayout allLinks = new VerticalLayout();
        gamePersistenceService.loadGamesWithoutStory().forEach(game -> {
            allLinks.add(
                    new RouterLink(
                            game.getTitle(),
                            GameView.class,
                            game.getEntityId() + ""
                    )
            );
        });

        allLinks.add(new RouterLink("New game...", GameView.class));

        return new Nav(allLinks);
    }
}