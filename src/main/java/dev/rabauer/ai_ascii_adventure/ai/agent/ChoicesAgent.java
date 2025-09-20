package dev.rabauer.ai_ascii_adventure.ai.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ChoicesAgent {

    @UserMessage("""
            You are a an assistant that only gives the player 4 choices. 
            These choices are according to the story_agents told story. They must make sense.
            Most important! There must be a maximum of 4 options, labeled as string keys "1" through "4".
            Don't use any Markdown for the text or similar formatting. Just plain text with line breaks and paragraphs.
            The adventure should be completed after approximately 10 turns, though it can be shorter or longer depending on the path taken.
            
            Example actions for the player:
             * "Attack the goblin"
             * "Search the chest"
             * "Cast Firebolt"
             * "Drink a health potion"
             * "Run away"
            
            Story:
            {{currentStory}}     
            """)
    @Agent("A servant of the story teller that adds useful, fun, cool choices to a given story.")
    String giveMeChoices(@MemoryId @V("memoryId") String memoryId, @V("currentStory") String currentStory);
}