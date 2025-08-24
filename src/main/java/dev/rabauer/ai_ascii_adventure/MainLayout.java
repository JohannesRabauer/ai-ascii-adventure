package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.orderedlayout.Scroller;
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
    }

    private Nav getSideNav() {
        Nav sideNav = new Nav();
        gamePersistenceService.loadGames().forEach(game -> {
            sideNav.add(
                    new RouterLink(
                            game.getTitle(),
                            ChatView.class,
                            game.getEntityId() + ""
                    )
            );
        });

        sideNav.add(new RouterLink("New game...", ChatView.class));

        return sideNav;
    }
}