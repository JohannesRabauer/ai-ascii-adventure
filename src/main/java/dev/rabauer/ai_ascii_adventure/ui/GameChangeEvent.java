package dev.rabauer.ai_ascii_adventure.ui;

import com.vaadin.flow.component.ComponentEvent;

public class GameChangeEvent extends ComponentEvent<GameView> {
    public GameChangeEvent(GameView source, boolean fromClient) {
        super(source, fromClient);
    }
}