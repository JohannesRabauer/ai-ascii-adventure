package dev.rabauer.ai_ascii_adventure.ai.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ToolAgent {

    @UserMessage("""
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
              {{currentStory}}     
            """)
    @Agent("A interface between story teller and java methods. You are here to call the correct java methods if necessary.")
    String callATool(@V("currentStory") String currentStory);
}