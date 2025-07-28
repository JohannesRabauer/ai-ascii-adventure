package dev.rabauer.ai_ascii_adventure.dto;

public record Game(Hero hero, Story story) {
    public static final String INITIAL_PROMPT =
            """
                       Generate an interactive fantasy text adventure game starring a hero named %s. The game should be played turn by turn, with each turn offering a description of the current scene and allowing the player to choose what Hero does next.
                               Game Rules:
                                   - Hero is a brave hero exploring a mysterious fantasy world filled with magic, monsters, and secrets.
                                   - Hero has the following stats:
                                       * Life: 100 (if it reaches 0, Hero dies and the game ends)
                                       * Mana: 50 (used to cast spells or perform magical actions)
                                       * Inventory: Starts empty but can be filled with items, weapons, potions, artifacts, etc.
                                   - Each turn, describe:
                                       * The current location and atmosphere
                                       * Any characters, enemies, items, or mysteries present
                                       * Hero’ current status (life, mana, inventory)
                                       * Present 2–4 clear choices for the player, or allow freeform input
                                   - The player can input actions like:
                                       * "Attack the goblin"
                                       * "Search the chest"
                                       * "Cast Firebolt"
                                       * "Drink a health potion"
                                       * "Run away"
                                       * Or make decisions like "Go north" or "Talk to the wizard"
                                   - Keep track of Hero' health, mana, and inventory throughout the adventure.
                                   - Let the story unfold based on the player’s choices, with real consequences (combat, traps, treasures, allies, etc.).
                                   - The adventure should be completed after approximately 15 turns, though it can be shorter or longer depending on the path taken.
                                   - Ensure a satisfying ending (victory, defeat, or an ambiguous fate) based on how the story unfolds.
                               
                               Tone and Setting:
                                   - Classic high-fantasy world: enchanted forests, forgotten ruins, ancient magic, mythical creatures.
                                   - Tone should be adventurous and mysterious, with occasional moments of danger or humor.
                               
                               First step:
                               Begin the story with Hero standing at the edge of a dense, fog-covered forest. His quest is unknown — he must discover it as he explores. 
                    """;

}
