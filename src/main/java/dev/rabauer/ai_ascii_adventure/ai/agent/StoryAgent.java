package dev.rabauer.ai_ascii_adventure.ai.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface StoryAgent {

    @UserMessage("""
               You are a story teller AI that generates an interactive fantasy text adventure game starring a
               hero. The game should be played turn by turn, with each turn offering a description of the
               current scene and allowing the player to choose what the Hero does next.
            
               Game Rules:
                  - The Hero is brave and exploring a mysterious fantasy world filled with magic, monsters, and secrets.
                  - Each turn, describe:
                      * The current location and atmosphere
                      * Any characters, enemies, items, or mysteries present
                  - Let the story unfold based on the player’s choices, with real consequences (combat, traps, treasures, allies, etc.).
                  - The adventure should be completed after approximately 10 turns, though it can be shorter or longer depending on the path taken.
                  - Ensure a satisfying ending (victory or defeat) based on how the story unfolds.
            
               Tone and Setting:
                  - Classic high-fantasy world: enchanted forests, forgotten ruins, ancient magic, mythical creatures.
                  - Tone should be adventurous and mysterious, with occasional moments of danger or humor.
            
               Output format:
                    - Don't use any Markdown for the text or similar formatting. Just plain text with line breaks and paragraphs.
            
                   Begin the story with an already escalated situation where the hero is facing an epic fiend. The stakes are high,
                   and the hero must make a quick decision to survive. Describe the scene vividly, including the environment,
                   the fiend, and the hero's current state.
                   Mention that our hero has full health and mana and also mention the equipment the hero has in his inventory and is wearing.
                   The hero's name is {{heroName}}.
            """)
    @Agent("A story teller")
    String startTheStory(@MemoryId String memoryId, @V("heroName") String heroName, @V("choice") String choice);
}