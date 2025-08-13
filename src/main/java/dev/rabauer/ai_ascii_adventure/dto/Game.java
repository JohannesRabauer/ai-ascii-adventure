package dev.rabauer.ai_ascii_adventure.dto;

public record Game(Hero hero, Story story) {
    public static final String INITIAL_PROMPT = """
               Step 1:
                   Initialize the Hero name with "%s", with full health, mana and some random items in the inventory.
               Step 2:
                   Begin the story with an already escalated situation where the hero is facing an epic fiend. The stakes are high,
                   and the hero must make a quick decision to survive. Describe the scene vividly, including the environment,
                   the fiend, and the hero's current state.
            """;

}
