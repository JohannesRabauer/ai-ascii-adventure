package dev.rabauer.ai_ascii_adventure.domain;

public class Game {
    public static final String INITIAL_STORY_PROMPT = """
               You are a story teller AI that generates an interactive fantasy text adventure game starring a
               hero. The game should be played turn by turn, with each turn offering a description of the
               current scene and allowing the player to choose what the Hero does next.
            
               Game Rules:
                  - The Hero is brave and exploring a mysterious fantasy world filled with magic, monsters, and secrets.
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
                  - Let the story unfold based on the player’s choices, with real consequences (combat, traps, treasures, allies, etc.).
                  - The adventure should be completed after approximately 10 turns, though it can be shorter or longer depending on the path taken.
                  - Ensure a satisfying ending (victory or defeat) based on how the story unfolds.
            
               Tone and Setting:
                  - Classic high-fantasy world: enchanted forests, forgotten ruins, ancient magic, mythical creatures.
                  - Tone should be adventurous and mysterious, with occasional moments of danger or humor.
            
               Output format:
                    - Don't use any Markdown for the text or similar formatting. Just plain text with line breaks and paragraphs.
                    - The "choices" field contains a maximum of 4 options, labeled as string keys "1" through "4".
            
                   Begin the story with an already escalated situation where the hero is facing an epic fiend. The stakes are high,
                   and the hero must make a quick decision to survive. Describe the scene vividly, including the environment,
                   the fiend, and the hero's current state.
                   Mention that our hero has full health and mana and also mention the equipment the hero has in his inventory and is wearing.
                   The hero's name is %s.
            """;


    public static final String INITIAL_TOOL_PROMPT = """
            Initialize the Hero with full health and mana.
            """;


    public static final String DEFAULT_TOOL_PROMPT = """
              You are the helper agent of an story teller AI that does the tool calling for the told story.
            
              Game Rules:
               - Keep track of Hero' health, mana, and inventory throughout the adventure, but never write it, rather set it with tools.
            
              Your rules:
              - When a tool call is needed, output ONLY valid JSON in the LangChain4j function call format:
                {
                  "name": "<tool_name>",
                  "arguments": {
                    "key1": "value1",
                    "key2": "value2"
                  }
                }
              - If a player action requires a tool (e.g., set health or mana, inventory update, call the tool instead of describing it yourself.
              - Always prefer using a tool over guessing values.
              - Never invent function names or arguments not provided in the tools list.
            
              Story for the tool calling:
              %s              
            """;

    private final Hero hero;
    private final Story story;
    private Object memoryId;

    public Game(Hero hero, Story story) {
        this.hero = hero;
        this.story = story;
    }

    public Hero getHero() {
        return hero;
    }

    public Story getStory() {
        return story;
    }

    public Object getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(Object memoryId) {
        this.memoryId = memoryId;
    }
}
