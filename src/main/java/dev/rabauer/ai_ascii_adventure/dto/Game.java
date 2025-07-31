package dev.rabauer.ai_ascii_adventure.dto;

public record Game(Hero hero, Story story) {
    public static final String INITIAL_PROMPT =
            """
                       Generate an interactive fantasy text adventure game starring a hero named %s. The game should be played turn by turn, with each turn offering a description of the current scene and allowing the player to choose what Hero does next.
                               Game Rules:
                                   - Hero is a brave hero exploring a mysterious fantasy world filled with magic, monsters, and secrets.
                                   - The hero has the following stats which you can use through function calls/tools:
                                       * Life 
                                       * Mana 
                                       * Inventory: Starts empty but can be filled with items, weapons, potions, artifacts, etc.
                                   - Each turn, describe:
                                       * The current location and atmosphere
                                       * Any characters, enemies, items, or mysteries present
                                       * Present 2–4 clear choices for the player, labelled 1 through 4
                                   - Example actions for the player:
                                       * "Attack the goblin"
                                       * "Search the chest"
                                       * "Cast Firebolt"
                                       * "Drink a health potion"
                                       * "Run away"
                                   - Keep track of Hero' health, mana, and inventory throughout the adventure, but never write it, rather set it with tools.
                                   - Let the story unfold based on the player’s choices, with real consequences (combat, traps, treasures, allies, etc.).
                                   - The adventure should be completed after approximately 15 turns, though it can be shorter or longer depending on the path taken.
                                   - Ensure a satisfying ending (victory, defeat, or an ambiguous fate) based on how the story unfolds.
                               
                               Tone and Setting:
                                   - Classic high-fantasy world: enchanted forests, forgotten ruins, ancient magic, mythical creatures.
                                   - Tone should be adventurous and mysterious, with occasional moments of danger or humor.
                                   
                               Output format:
                                     - Don't use any Markdown for the text or similar formatting. Just plain text with line breaks and paragraphs.
                                     - The "choices" field contains a maximum of 4 options, labeled as string keys "1" through "4".
                               
                               First step:
                               Begin the story with Hero standing at the edge of a dense, fog-covered forest. His quest is unknown — he must discover it as he explores. 
                    """;

}
